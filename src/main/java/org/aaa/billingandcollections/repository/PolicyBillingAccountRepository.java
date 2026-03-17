package org.aaa.billingandcollections.repository;

import jakarta.annotation.PostConstruct;
import org.aaa.billingandcollections.model.DelinquencyStatus;
import org.aaa.billingandcollections.model.entities.PolicyBillingAccount;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PolicyBillingAccountRepository {

    private final ConcurrentHashMap<String, PolicyBillingAccount> accounts = new ConcurrentHashMap<>();

    public PolicyBillingAccount save(PolicyBillingAccount account) {
        accounts.put(account.getPolicyId(), account);
        return account;
    }

    public Optional<PolicyBillingAccount> findByPolicyId(String policyId) {
        return Optional.ofNullable(accounts.get(policyId));
    }

    public List<PolicyBillingAccount> findAll() {
        return accounts.values().stream().toList();
    }

    public List<PolicyBillingAccount> findDelinquentPolicies() {
        return accounts.values().stream()
                .filter(account -> account.getDelinquencyStatus() == DelinquencyStatus.DELINQUENT)
                .toList();
    }

    @PostConstruct
    private void seedData() {
        save(PolicyBillingAccount.builder()
                .policyId("POLICY-1001")
                .delinquencyStatus(DelinquencyStatus.DELINQUENT)
                .delinquentSince(Instant.now().minusSeconds(10 * 24 * 60 * 60))
                .updatedAt(Instant.now())
                .outstandingAmount(new BigDecimal("120.00"))
                .build());

        save(PolicyBillingAccount.builder()
                .policyId("POLICY-2001")
                .delinquencyStatus(DelinquencyStatus.PAST_DUE)
                .delinquentSince(null)
                .updatedAt(Instant.now())
                .outstandingAmount(new BigDecimal("95.00"))
                .build());

        save(PolicyBillingAccount.builder()
                .policyId("POLICY-3001")
                .delinquencyStatus(DelinquencyStatus.CURRENT)
                .delinquentSince(null)
                .updatedAt(Instant.now())
                .outstandingAmount(BigDecimal.ZERO)
                .build());
    }
}