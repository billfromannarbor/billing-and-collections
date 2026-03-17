package org.aaa.billingandcollections.service;

import lombok.RequiredArgsConstructor;
import org.aaa.billingandcollections.dto.CreatePaymentRequest;
import org.aaa.billingandcollections.dto.CreatePaymentResponse;
import org.aaa.billingandcollections.model.PaymentStatus;
import org.aaa.billingandcollections.model.entities.Payment;
import org.aaa.billingandcollections.repository.PaymentRepository;
import org.aaa.billingandcollections.repository.PremiumScheduleItemRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Slf4j
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

        log.info("Created payment {} for policy {} and schedule item {} with status {}",
                payment.getPaymentId(),
                payment.getPolicyId(),
                payment.getScheduleItemId(),
                payment.getStatus());

        return new CreatePaymentResponse(payment.getPaymentId(), payment.getStatus());
    }
}