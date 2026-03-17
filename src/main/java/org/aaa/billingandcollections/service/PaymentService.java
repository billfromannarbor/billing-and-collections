package org.aaa.billingandcollections.service;

import lombok.RequiredArgsConstructor;
import org.aaa.billingandcollections.dto.CreatePaymentRequest;
import org.aaa.billingandcollections.dto.CreatePaymentResponse;
import org.aaa.billingandcollections.model.PaymentStatus;
import org.aaa.billingandcollections.model.entities.Payment;
import org.aaa.billingandcollections.repository.PaymentRepository;
import org.aaa.billingandcollections.repository.PremiumScheduleItemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PremiumScheduleItemRepository premiumScheduleItemRepository;

    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        var scheduleItem = premiumScheduleItemRepository.findById(request.scheduleItemId());

        if (scheduleItem == null) {
            throw new IllegalArgumentException("Schedule item not found: " + request.scheduleItemId());
        }

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .policyId(request.policyId())
                .scheduleItemId(request.scheduleItemId())
                .amount(request.amount())
                .status(PaymentStatus.PENDING)
                .attemptCount(0)
                .enqueued(false)
                .createdAt(Instant.now())
                .build();

        paymentRepository.save(payment);

        return new CreatePaymentResponse(payment.getPaymentId(), payment.getStatus());
    }
}