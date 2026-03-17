package org.aaa.billingandcollections.repository;

import org.aaa.billingandcollections.model.PaymentStatus;
import org.aaa.billingandcollections.model.entities.Payment;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PaymentRepository {

    private final ConcurrentHashMap<UUID, Payment> payments = new ConcurrentHashMap<>();

    public Payment save(Payment payment) {
        payments.put(payment.getPaymentId(), payment);
        return payment;
    }

    public Optional<Payment> findById(UUID paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }

    public Collection<Payment> findAll() {
        return payments.values();
    }

    public List<Payment> findReadyToDispatch(Instant now) {
        return payments.values().stream()
                .filter(payment -> !payment.isEnqueued())
                .filter(payment ->
                        payment.getStatus() == PaymentStatus.PENDING ||
                                (payment.getStatus() == PaymentStatus.RETRY_PENDING &&
                                        payment.getNextAttemptAt() != null &&
                                        !payment.getNextAttemptAt().isAfter(now))
                )
                .toList();
    }
}