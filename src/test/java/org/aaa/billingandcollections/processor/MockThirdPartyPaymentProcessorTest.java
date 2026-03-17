package org.aaa.billingandcollections.processor;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.aaa.billingandcollections.model.PaymentAttemptResult.FAILURE;
import static org.aaa.billingandcollections.model.PaymentAttemptResult.SUCCESS;

class MockThirdPartyPaymentProcessorTest {

    private final MockThirdPartyPaymentProcessor processor = new MockThirdPartyPaymentProcessor();

    @Test
    void processPayment_shouldFailOnFirstAttempt() {
        UUID paymentId = UUID.randomUUID();

        var result = processor.processPayment(paymentId);

        assertThat(result.result()).isEqualTo(FAILURE);
        assertThat(result.failureReason()).isNotNull();
    }

    @Test
    void processPayment_shouldSucceedOnSecondAttempt() {
        UUID paymentId = UUID.randomUUID();

        processor.processPayment(paymentId); // first attempt (fail)
        var result = processor.processPayment(paymentId); // second attempt

        assertThat(result.result()).isEqualTo(SUCCESS);
        assertThat(result.failureReason()).isNull();
    }

    @Test
    void processPayment_shouldTrackAttemptsPerPaymentId() {
        UUID paymentA = UUID.randomUUID();
        UUID paymentB = UUID.randomUUID();

        // First attempt for both → should fail
        var resultA1 = processor.processPayment(paymentA);
        var resultB1 = processor.processPayment(paymentB);

        assertThat(resultA1.result()).isEqualTo(FAILURE);
        assertThat(resultB1.result()).isEqualTo(FAILURE);

        // Second attempt for A → success
        var resultA2 = processor.processPayment(paymentA);

        assertThat(resultA2.result()).isEqualTo(SUCCESS);

        // B should still fail on its second call
        var resultB2 = processor.processPayment(paymentB);

        assertThat(resultB2.result()).isEqualTo(SUCCESS);
    }
}