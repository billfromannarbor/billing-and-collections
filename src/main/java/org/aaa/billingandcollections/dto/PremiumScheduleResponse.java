package org.aaa.billingandcollections.dto;

import org.aaa.billingandcollections.model.DelinquencyStatus;

import java.util.List;

public record PremiumScheduleResponse(
        String policyId,
        DelinquencyStatus delinquencyStatus,
        List<PremiumScheduleItemResponse> premiumSchedule
) {
}