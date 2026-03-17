package org.aaa.billingandcollections.worker;

import org.aaa.billingandcollections.model.PaymentAttemptResult;
import org.aaa.billingandcollections.model.PaymentStatus;
import org.aaa.billingandcollections.model.entities.Payment;
import org.aaa.billingandcollections.queue.PaymentQueue;
import org.aaa.billingandcollections.repository.PaymentAttemptRepository;
import org.aaa.billingandcollections.repository.PaymentRepository;
import org.aaa.billingandcollections.processor.MockThirdPartyPaymentProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentWorkerTest {

    private PaymentRepository paymentRepository;
    private PaymentAttemptRepository paymentAttemptRepository;
    private PaymentQueue paymentQueue;
    private MockThirdPartyPaymentProcessor mockProcessor;
    private PaymentWorker paymentWorker;

    @BeforeEach
    void setUp() {
        paymentRepository = new PaymentRepository();
        paymentAttemptRepository = new PaymentAttemptRepository();
        paymentQueue = new PaymentQueue();
        mockProcessor = new MockThirdPartyPaymentProcessor();

        paymentWorker = new PaymentWorker(
                paymentQueue,
                paymentRepository,
                paymentAttemptRepository,
                mockProcessor,
                2,
                2000L
        );
    }

    @Test
    void processNextPayment_shouldRecordFailureAndMarkRetryPendingOnFirstAttempt() {
        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .policyId("POLICY-1001")
                .scheduleItemId(UUID.randomUUID())
                .amount(new BigDecimal("120.00"))
                .status(PaymentStatus.PENDING)
                .attemptCount(0)
                .enqueued(true)
                .createdAt(Instant.now())
                .build();

        paymentRepository.save(payment);
        paymentQueue.enqueue(payment.getPaymentId());

        paymentWorker.processNextPayment();

        Payment updated = paymentRepository.findById(payment.getPaymentId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.RETRY_PENDING);
        assertThat(updated.getAttemptCount()).isEqualTo(1);
        assertThat(updated.isEnqueued()).isFalse();
        assertThat(updated.getNextAttemptAt()).isNotNull();

        var attempts = paymentAttemptRepository.findByPaymentId(payment.getPaymentId());
        assertThat(attempts).hasSize(1);
        assertThat(attempts.getFirst().getResult()).isEqualTo(PaymentAttemptResult.FAILURE);
    }

    @Test
    void processNextPayment_shouldRecordSuccessOnSecondAttempt() {
        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .policyId("POLICY-1001")
                .scheduleItemId(UUID.randomUUID())
                .amount(new BigDecimal("120.00"))
                .status(PaymentStatus.PENDING)
                .attemptCount(0)
                .enqueued(true)
                .createdAt(Instant.now())
                .build();

        paymentRepository.save(payment);

        // first attempt -> failure
        paymentQueue.enqueue(payment.getPaymentId());
        paymentWorker.processNextPayment();

        Payment afterFirstAttempt = paymentRepository.findById(payment.getPaymentId()).orElseThrow();
        assertThat(afterFirstAttempt.getStatus()).isEqualTo(PaymentStatus.RETRY_PENDING);

        // simulate dispatcher re-enqueue for retry
        afterFirstAttempt.setEnqueued(true);
        paymentRepository.save(afterFirstAttempt);

        paymentQueue.enqueue(payment.getPaymentId());
        paymentWorker.processNextPayment();

        Payment updated = paymentRepository.findById(payment.getPaymentId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(updated.getAttemptCount()).isEqualTo(2);
        assertThat(updated.isEnqueued()).isFalse();
        assertThat(updated.getLastAttemptAt()).isNotNull();

        var attempts = paymentAttemptRepository.findByPaymentId(payment.getPaymentId());
        assertThat(attempts).hasSize(2);
        assertThat(attempts.get(0).getResult()).isEqualTo(PaymentAttemptResult.FAILURE);
        assertThat(attempts.get(1).getResult()).isEqualTo(PaymentAttemptResult.SUCCESS);
    }
}