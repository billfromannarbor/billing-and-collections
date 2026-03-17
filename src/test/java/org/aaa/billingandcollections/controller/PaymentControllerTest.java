package org.aaa.billingandcollections.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aaa.billingandcollections.dto.CreatePaymentRequest;
import org.aaa.billingandcollections.dto.CreatePaymentResponse;
import org.aaa.billingandcollections.model.PaymentStatus;
import org.aaa.billingandcollections.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    void createPayment_shouldReturnAccepted() throws Exception {
        UUID paymentId = UUID.randomUUID();

        when(paymentService.createPayment(any(CreatePaymentRequest.class)))
                .thenReturn(new CreatePaymentResponse(paymentId, PaymentStatus.PENDING));

        var request = new CreatePaymentRequest(
                "POLICY-1001",
                UUID.randomUUID(),
                new BigDecimal("120.00")
        );

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}