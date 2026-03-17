package org.aaa.billingandcollections.worker;

import lombok.extern.slf4j.Slf4j;
import org.aaa.billingandcollections.model.PaymentAttemptResult;
import org.aaa.billingandcollections.model.PaymentStatus;
import org.aaa.billingandcollections.model.entities.Payment;
import org.aaa.billingandcollections.model.entities.PaymentAttempt;
import org.aaa.billingandcollections.processor.MockPaymentProcessorResult;
import org.aaa.billingandcollections.processor.MockThirdPartyPaymentProcessor;
import org.aaa.billingandcollections.queue.PaymentQueue;
import org.aaa.billingandcollections.repository.PaymentAttemptRepository;
import org.aaa.billingandcollections.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class PaymentWorker {

    private final PaymentQueue paymentQueue;
    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final MockThirdPartyPaymentProcessor paymentProcessor;
    private final int maxAttempts;
    private final long retryDelayMs;

    public PaymentWorker(
            PaymentQueue paymentQueue,
            PaymentRepository paymentRepository,
            PaymentAttemptRepository paymentAttemptRepository,
            MockThirdPartyPaymentProcessor paymentProcessor,
            @Value("${payment.retry.max-attempts}") int maxAttempts,
            @Value("${payment.retry.delay-ms}") long retryDelayMs
    ) {
        this.paymentQueue = paymentQueue;
        this.paymentRepository = paymentRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.paymentProcessor = paymentProcessor;
        this.maxAttempts = maxAttempts;
        this.retryDelayMs = retryDelayMs;
    }

    @Scheduled(fixedDelayString = "${payment.worker.fixed-delay-ms}")
    public void processNextPayment() {
        UUID paymentId = paymentQueue.poll();

        if (paymentId == null) {
            return;
        }

        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);

        if (paymentOptional.isEmpty()) {
            log.warn("Payment {} not found while attempting to process queue item", paymentId);
            return;
        }

        Payment payment = paymentOptional.get();

        log.info("Processing payment {} with current status {} and attempt count {}",
                payment.getPaymentId(),
                payment.getStatus(),
                payment.getAttemptCount());

        int attemptNumber = payment.getAttemptCount() + 1;

        MockPaymentProcessorResult processorResult = paymentProcessor.processPayment(paymentId);

        PaymentAttempt attempt = PaymentAttempt.builder()
                .paymentAttemptId(UUID.randomUUID())
                .paymentId(paymentId)
                .attemptNumber(attemptNumber)
                .result(processorResult.result())
                .failureReason(processorResult.failureReason())
                .attemptedAt(Instant.now())
                .build();

        paymentAttemptRepository.save(attempt);

        log.info("Recorded payment attempt {} for payment {} with result {}",
                attempt.getAttemptNumber(),
                paymentId,
                attempt.getResult());

        payment.setAttemptCount(attemptNumber);
        payment.setLastAttemptAt(Instant.now());
        payment.setEnqueued(false);

        if (processorResult.result() == PaymentAttemptResult.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setNextAttemptAt(null);

            log.info("Payment {} succeeded on attempt {}",
                    paymentId,
                    attemptNumber);
        } else {
            if (attemptNumber < maxAttempts) {
                payment.setStatus(PaymentStatus.RETRY_PENDING);
                payment.setNextAttemptAt(Instant.now().plusMillis(retryDelayMs));

                log.info("Payment {} failed on attempt {} and was marked RETRY_PENDING for retry at {}",
                        paymentId,
                        attemptNumber,
                        payment.getNextAttemptAt());
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setNextAttemptAt(null);

                log.info("Payment {} failed on final attempt {}",
                        paymentId,
                        attemptNumber);
            }
        }

        paymentRepository.save(payment);
    }
}