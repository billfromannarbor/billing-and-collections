package org.aaa.billingandcollections.processor;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.aaa.billingandcollections.model.PaymentAttemptResult.FAILURE;
import static org.aaa.billingandcollections.model.PaymentAttemptResult.SUCCESS;

@Component
public class MockThirdPartyPaymentProcessor {

    private final ConcurrentHashMap<UUID, AtomicInteger> attemptTracker = new ConcurrentHashMap<>();

    public MockPaymentProcessorResult processPayment(UUID paymentId) {
        int attemptNumber = attemptTracker
                .computeIfAbsent(paymentId, id -> new AtomicInteger(0))
                .incrementAndGet();

        if (attemptNumber == 1) {
            return new MockPaymentProcessorResult(FAILURE, "Mock processor failure on first attempt");
        }

        return new MockPaymentProcessorResult(SUCCESS, null);
    }
}