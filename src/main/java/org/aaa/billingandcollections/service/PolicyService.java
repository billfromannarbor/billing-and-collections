package org.aaa.billingandcollections.service;

import lombok.RequiredArgsConstructor;
import org.aaa.billingandcollections.model.entities.PolicyBillingAccount;
import org.aaa.billingandcollections.model.entities.PremiumScheduleItem;
import org.aaa.billingandcollections.repository.PolicyBillingAccountRepository;
import org.aaa.billingandcollections.repository.PremiumScheduleItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PremiumScheduleItemRepository premiumScheduleItemRepository;
    private final PolicyBillingAccountRepository policyBillingAccountRepository;

    public List<PremiumScheduleItem> getPremiumSchedule(String policyId) {
        return premiumScheduleItemRepository.findByPolicyId(policyId);
    }

    public List<PolicyBillingAccount> getDelinquentPolicies() {
        return policyBillingAccountRepository.findDelinquentPolicies();
    }
}