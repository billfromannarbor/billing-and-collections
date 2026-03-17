package org.aaa.billingandcollections.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aaa.billingandcollections.model.DelinquencyStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyBillingAccount {
    private String policyId;
    private DelinquencyStatus delinquencyStatus;
    private Instant delinquentSince;
    private Instant updatedAt;
    private BigDecimal outstandingAmount;
}
