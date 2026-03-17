package org.aaa.billingandcollections.service;

import org.aaa.billingandcollections.dto.CreatePaymentRequest;
import org.aaa.billingandcollections.model.PaymentStatus;
import org.aaa.billingandcollections.model.ScheduleItemStatus;
import org.aaa.billingandcollections.model.entities.PremiumScheduleItem;
import org.aaa.billingandcollections.repository.PaymentRepository;
import org.aaa.billingandcollections.repository.PremiumScheduleItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private PremiumScheduleItemRepository premiumScheduleItemRepository;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = new PaymentRepository();
        premiumScheduleItemRepository = new PremiumScheduleItemRepository();

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

        paymentService = new PaymentService(paymentRepository, premiumScheduleItemRepository);
    }

    @Test
    void createPayment_shouldCreatePendingPayment() {
        var scheduleItem = premiumScheduleItemRepository.findByPolicyId("POLICY-1001").getFirst();

        var request = new CreatePaymentRequest(
                "POLICY-1001",
                scheduleItem.getScheduleItemId(),
                new BigDecimal("120.00")
        );

        var response = paymentService.createPayment(request);

        var savedPayment = paymentRepository.findById(response.paymentId()).orElseThrow();

        assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(savedPayment.isEnqueued()).isFalse();
        assertThat(savedPayment.getAttemptCount()).isZero();
        assertThat(savedPayment.getPolicyId()).isEqualTo("POLICY-1001");
        assertThat(savedPayment.getScheduleItemId()).isEqualTo(scheduleItem.getScheduleItemId());
    }

    @Test
    void createPayment_shouldThrowWhenScheduleItemDoesNotExist() {
        var request = new CreatePaymentRequest(
                "POLICY-1001",
                UUID.randomUUID(),
                new BigDecimal("120.00")
        );

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule item not found");
    }
}