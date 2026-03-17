package org.aaa.billingandcollections.controller;

import lombok.RequiredArgsConstructor;
import org.aaa.billingandcollections.model.entities.PolicyBillingAccount;
import org.aaa.billingandcollections.model.entities.PremiumScheduleItem;
import org.aaa.billingandcollections.service.PolicyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping("/{policyId}/premium-schedule")
    public List<PremiumScheduleItem> getPremiumSchedule(@PathVariable String policyId) {
        return policyService.getPremiumSchedule(policyId);
    }

    @GetMapping("/delinquent")
    public List<PolicyBillingAccount> getDelinquentPolicies() {
        return policyService.getDelinquentPolicies();
    }
}