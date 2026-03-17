package org.aaa.billingandcollections;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aaa.billingandcollections.dto.CreatePaymentRequest;
import org.aaa.billingandcollections.model.PaymentAttemptResult;
import org.aaa.billingandcollections.model.PaymentStatus;
import org.aaa.billingandcollections.model.entities.Payment;
import org.aaa.billingandcollections.model.entities.PaymentAttempt;
import org.aaa.billingandcollections.repository.PaymentAttemptRepository;
import org.aaa.billingandcollections.repository.PaymentRepository;
import org.aaa.billingandcollections.repository.PremiumScheduleItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PremiumScheduleItemRepository premiumScheduleItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @Test
    void paymentShouldFailOnceThenRetryAndSucceed() throws Exception {
        var scheduleItem = premiumScheduleItemRepository.findByPolicyId("POLICY-1001").getFirst();

        var request = new CreatePaymentRequest(
                "POLICY-1001",
                scheduleItem.getScheduleItemId(),
                new BigDecimal("120.00")
        );

        String responseBody = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID paymentId = objectMapper.readTree(responseBody)
                .get("paymentId")
                .traverse(objectMapper)
                .readValueAs(UUID.class);

        await().untilAsserted(() -> {
            Payment payment = paymentRepository.findById(paymentId).orElseThrow();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
            assertThat(payment.getAttemptCount()).isEqualTo(2);
            assertThat(payment.isEnqueued()).isFalse();
        });

        await().untilAsserted(() -> {
            List<PaymentAttempt> attempts = paymentAttemptRepository.findByPaymentId(paymentId);

            assertThat(attempts).hasSize(2);

            List<PaymentAttempt> sortedAttempts = attempts.stream()
                    .sorted(Comparator.comparingInt(PaymentAttempt::getAttemptNumber))
                    .toList();

            assertThat(sortedAttempts.get(0).getResult()).isEqualTo(PaymentAttemptResult.FAILURE);
            assertThat(sortedAttempts.get(1).getResult()).isEqualTo(PaymentAttemptResult.SUCCESS);
        });
    }
}