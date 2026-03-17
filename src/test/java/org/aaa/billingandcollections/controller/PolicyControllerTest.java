package org.aaa.billingandcollections.controller;

import org.aaa.billingandcollections.model.DelinquencyStatus;
import org.aaa.billingandcollections.model.ScheduleItemStatus;
import org.aaa.billingandcollections.model.entities.PolicyBillingAccount;
import org.aaa.billingandcollections.model.entities.PremiumScheduleItem;
import org.aaa.billingandcollections.service.PolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyService policyService;

    @Test
    void getPremiumSchedule_shouldReturnOk() throws Exception {
        when(policyService.getPremiumSchedule("POLICY-1001"))
                .thenReturn(List.of(
                        PremiumScheduleItem.builder()
                                .scheduleItemId(UUID.randomUUID())
                                .policyId("POLICY-1001")
                                .dueDate(LocalDate.now())
                                .amount(new BigDecimal("120.00"))
                                .status(ScheduleItemStatus.DUE)
                                .build()
                ));

        mockMvc.perform(get("/policies/POLICY-1001/premium-schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].policyId").value("POLICY-1001"));
    }

    @Test
    void getDelinquentPolicies_shouldReturnOk() throws Exception {
        when(policyService.getDelinquentPolicies())
                .thenReturn(List.of(
                        PolicyBillingAccount.builder()
                                .policyId("POLICY-1001")
                                .delinquencyStatus(DelinquencyStatus.DELINQUENT)
                                .delinquentSince(Instant.now())
                                .outstandingAmount(new BigDecimal("120.00"))
                                .updatedAt(Instant.now())
                                .build()
                ));

        mockMvc.perform(get("/policies/delinquent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].policyId").value("POLICY-1001"))
                .andExpect(jsonPath("$[0].delinquencyStatus").value("DELINQUENT"));
    }
}