package org.aaa.billingandcollections.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aaa.billingandcollections.model.PaymentAttemptResult;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAttempt {
    private UUID paymentAttemptId;
    private UUID paymentId;
    private int attemptNumber;

    private PaymentAttemptResult result;
    private String failureReason;

    private Instant attemptedAt;
}
