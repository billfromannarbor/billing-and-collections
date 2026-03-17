package org.aaa.billingandcollections.repository;

import org.aaa.billingandcollections.model.entities.PaymentAttempt;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PaymentAttemptRepository {

    private final ConcurrentHashMap<UUID, PaymentAttempt> attempts = new ConcurrentHashMap<>();

    public PaymentAttempt save(PaymentAttempt attempt) {
        attempts.put(attempt.getPaymentAttemptId(), attempt);
        return attempt;
    }

    public Optional<PaymentAttempt> findById(UUID attemptId) {
        return Optional.ofNullable(attempts.get(attemptId));
    }

    public List<PaymentAttempt> findByPaymentId(UUID paymentId) {
        return attempts.values()
                .stream()
                .filter(a -> a.getPaymentId().equals(paymentId))
                .toList();
    }

    public List<PaymentAttempt> findAll() {
        return attempts.values().stream().toList();
    }
}