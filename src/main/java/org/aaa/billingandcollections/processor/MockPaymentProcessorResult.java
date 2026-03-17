package org.aaa.billingandcollections.processor;

import org.aaa.billingandcollections.model.PaymentAttemptResult;

public record MockPaymentProcessorResult(
        PaymentAttemptResult result,
        String failureReason
) {
}