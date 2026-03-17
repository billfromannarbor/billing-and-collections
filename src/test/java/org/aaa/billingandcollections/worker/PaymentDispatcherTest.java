package org.aaa.billingandcollections.worker;

import org.aaa.billingandcollections.model.PaymentStatus;
import org.aaa.billingandcollections.model.entities.Payment;
import org.aaa.billingandcollections.queue.PaymentQueue;
import org.aaa.billingandcollections.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentDispatcherTest {

    private PaymentRepository paymentRepository;
    private PaymentQueue paymentQueue;
    private PaymentDispatcher paymentDispatcher;

    @BeforeEach
    void setUp() {
        paymentRepository = new PaymentRepository();
        paymentQueue = new PaymentQueue();
        paymentDispatcher = new PaymentDispatcher(paymentRepository, paymentQueue);
    }

    @Test
    void dispatchEligiblePayments_shouldEnqueuePendingPayment() {
        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .policyId("POLICY-1001")
                .scheduleItemId(UUID.randomUUID())
                .amount(new BigDecimal("120.00"))
                .status(PaymentStatus.PENDING)
                .attemptCount(0)
                .enqueued(false)
                .createdAt(Instant.now())
                .build();

        paymentRepository.save(payment);

        paymentDispatcher.dispatchEligiblePayments();

        Payment saved = paymentRepository.findById(payment.getPaymentId()).orElseThrow();

        assertThat(saved.isEnqueued()).isTrue();
        assertThat(paymentQueue.size()).isEqualTo(1);
    }

    @Test
    void dispatchEligiblePayments_shouldEnqueueRetryPendingPaymentWhenNextAttemptAtReached() {
        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .policyId("POLICY-1001")
                .scheduleItemId(UUID.randomUUID())
                .amount(new BigDecimal("120.00"))
                .status(PaymentStatus.RETRY_PENDING)
                .attemptCount(1)
                .enqueued(false)
                .createdAt(Instant.now())
                .nextAttemptAt(Instant.now().minusSeconds(5))
                .build();

        paymentRepository.save(payment);

        paymentDispatcher.dispatchEligiblePayments();

        Payment saved = paymentRepository.findById(payment.getPaymentId()).orElseThrow();

        assertThat(saved.isEnqueued()).isTrue();
        assertThat(paymentQueue.size()).isEqualTo(1);
    }

    @Test
    void dispatchEligiblePayments_shouldNotEnqueueRetryPendingPaymentBeforeNextAttemptAt() {
        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .policyId("POLICY-1001")
                .scheduleItemId(UUID.randomUUID())
                .amount(new BigDecimal("120.00"))
                .status(PaymentStatus.RETRY_PENDING)
                .attemptCount(1)
                .enqueued(false)
                .createdAt(Instant.now())
                .nextAttemptAt(Instant.now().plusSeconds(60))
                .build();

        paymentRepository.save(payment);

        paymentDispatcher.dispatchEligiblePayments();

        Payment saved = paymentRepository.findById(payment.getPaymentId()).orElseThrow();

        assertThat(saved.isEnqueued()).isFalse();
        assertThat(paymentQueue.size()).isZero();
    }

    @Test
    void dispatchEligiblePayments_shouldNotEnqueueAlreadyEnqueuedPayment() {
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

        paymentDispatcher.dispatchEligiblePayments();

        assertThat(paymentQueue.size()).isZero();
    }
}