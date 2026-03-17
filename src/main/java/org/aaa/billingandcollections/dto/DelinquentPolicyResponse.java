package org.aaa.billingandcollections.dto;

import org.aaa.billingandcollections.model.DelinquencyStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record DelinquentPolicyResponse(
        String policyId,
        DelinquencyStatus delinquencyStatus,
        Instant delinquentSince,
        BigDecimal outstandingAmount
) {
}