package org.aaa.billingandcollections.queue;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class PaymentQueue {

    private final BlockingQueue<UUID> queue = new LinkedBlockingQueue<>();

    public void enqueue(UUID paymentId) {
        queue.offer(paymentId);
    }

    public UUID poll() {
        return queue.poll();
    }

    public int size() {
        return queue.size();
    }
}