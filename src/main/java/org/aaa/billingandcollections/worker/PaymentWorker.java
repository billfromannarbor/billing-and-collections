package org.aaa.billingandcollections.worker;

import lombok.RequiredArgsConstructor;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentWorker {

    private static final int MAX_ATTEMPTS = 2;

    private final PaymentQueue paymentQueue;
    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final MockThirdPartyPaymentProcessor paymentProcessor;

    @Scheduled(fixedDelay = 1000)
    public void processNextPayment() {
        UUID paymentId = paymentQueue.poll();

        if (paymentId == null) {
            return;
        }

        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);

        if (paymentOptional.isEmpty()) {
            log.warn("Payment {} not found", paymentId);
            return;
        }

        Payment payment = paymentOptional.get();
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

        payment.setAttemptCount(attemptNumber);
        payment.setLastAttemptAt(Instant.now());
        payment.setEnqueued(false);

        if (processorResult.result() == PaymentAttemptResult.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setNextAttemptAt(null);
            log.info("Payment {} succeeded on attempt {}", paymentId, attemptNumber);
        } else {
            if (attemptNumber < MAX_ATTEMPTS) {
                payment.setStatus(PaymentStatus.RETRY_PENDING);
                payment.setNextAttemptAt(Instant.now().plusSeconds(2));
                log.info("Payment {} failed on attempt {}, marked for retry", paymentId, attemptNumber);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setNextAttemptAt(null);
                log.info("Payment {} failed on final attempt {}", paymentId, attemptNumber);
            }
        }

        paymentRepository.save(payment);
    }
}