package org.aaa.billingandcollections.dto;

import org.aaa.billingandcollections.model.PaymentStatus;

import java.util.UUID;

public record CreatePaymentResponse(
        UUID paymentId,
        PaymentStatus status
) {
}