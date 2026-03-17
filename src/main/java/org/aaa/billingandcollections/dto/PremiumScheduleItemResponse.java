package org.aaa.billingandcollections.dto;

import org.aaa.billingandcollections.model.ScheduleItemStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PremiumScheduleItemResponse(
        UUID scheduleItemId,
        LocalDate dueDate,
        BigDecimal amount,
        ScheduleItemStatus status
) {
}