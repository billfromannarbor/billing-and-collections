package org.aaa.billingandcollections.service;

import org.aaa.billingandcollections.model.DelinquencyStatus;
import org.aaa.billingandcollections.model.ScheduleItemStatus;
import org.aaa.billingandcollections.model.entities.PolicyBillingAccount;
import org.aaa.billingandcollections.model.entities.PremiumScheduleItem;
import org.aaa.billingandcollections.repository.PolicyBillingAccountRepository;
import org.aaa.billingandcollections.repository.PremiumScheduleItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyServiceTest {

    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        var premiumScheduleItemRepository = new PremiumScheduleItemRepository();
        var policyBillingAccountRepository = new PolicyBillingAccountRepository();

        premiumScheduleItemRepository.save(PremiumScheduleItem.builder()
                .scheduleItemId(UUID.randomUUID())
                .policyId("POLICY-1001")
                .dueDate(LocalDate.now().minusDays(5))
                .amount(new BigDecimal("120.00"))
                .status(ScheduleItemStatus.OVERDUE)
                .build());

        premiumScheduleItemRepository.save(PremiumScheduleItem.builder()
                .scheduleItemId(UUID.randomUUID())
                .policyId("POLICY-1001")
                .dueDate(LocalDate.now().plusDays(25))
                .amount(new BigDecimal("120.00"))
                .status(ScheduleItemStatus.FUTURE)
                .build());

        policyBillingAccountRepository.save(PolicyBillingAccount.builder()
                .policyId("POLICY-1001")
                .delinquencyStatus(DelinquencyStatus.DELINQUENT)
                .delinquentSince(Instant.now().minusSeconds(86400))
                .updatedAt(Instant.now())
                .outstandingAmount(new BigDecimal("120.00"))
                .build());

        policyBillingAccountRepository.save(PolicyBillingAccount.builder()
                .policyId("POLICY-2001")
                .delinquencyStatus(DelinquencyStatus.CURRENT)
                .delinquentSince(null)
                .updatedAt(Instant.now())
                .outstandingAmount(BigDecimal.ZERO)
                .build());

        policyService = new PolicyService(
                premiumScheduleItemRepository,
                policyBillingAccountRepository
        );
    }

    @Test
    void getPremiumSchedule_shouldReturnScheduleItemsForPolicy() {
        var schedule = policyService.getPremiumSchedule("POLICY-1001");

        assertThat(schedule).isNotEmpty();
        assertThat(schedule).allMatch(item -> item.getPolicyId().equals("POLICY-1001"));
    }

    @Test
    void getDelinquentPolicies_shouldReturnOnlyDelinquentPolicies() {
        var delinquentPolicies = policyService.getDelinquentPolicies();

        assertThat(delinquentPolicies).isNotEmpty();
        assertThat(delinquentPolicies)
                .allMatch(policy -> policy.getDelinquencyStatus() == DelinquencyStatus.DELINQUENT);
    }
}