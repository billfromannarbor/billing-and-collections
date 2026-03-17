package org.aaa.billingandcollections.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aaa.billingandcollections.model.entities.Payment;
import org.aaa.billingandcollections.queue.PaymentQueue;
import org.aaa.billingandcollections.repository.PaymentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentDispatcher {

    private final PaymentRepository paymentRepository;
    private final PaymentQueue paymentQueue;

    @Scheduled(fixedDelay = 1000)
    public void dispatchEligiblePayments() {
        List<Payment> eligiblePayments = paymentRepository.findReadyToDispatch(Instant.now());

        for (Payment payment : eligiblePayments) {
            paymentQueue.enqueue(payment.getPaymentId());
            payment.setEnqueued(true);
            paymentRepository.save(payment);

            log.info("Dispatched payment {} to queue", payment.getPaymentId());
        }
    }
}