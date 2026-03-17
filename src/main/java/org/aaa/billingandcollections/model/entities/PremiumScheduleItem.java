package org.aaa.billingandcollections.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aaa.billingandcollections.model.ScheduleItemStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumScheduleItem {
    private UUID scheduleItemId;
    private String policyId;
    private LocalDate dueDate;
    private BigDecimal amount;
    private ScheduleItemStatus status;
}
