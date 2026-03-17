package org.aaa.billingandcollections.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aaa.billingandcollections.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private UUID paymentId;
    private String policyId;
    private UUID scheduleItemId;
    private BigDecimal amount;

    private PaymentStatus status;

    private int attemptCount;
    private Instant lastAttemptAt;
    private Instant nextAttemptAt;

    private boolean enqueued;
    private Instant createdAt;
}
