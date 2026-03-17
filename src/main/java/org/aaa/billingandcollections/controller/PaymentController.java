package org.aaa.billingandcollections.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aaa.billingandcollections.dto.CreatePaymentRequest;
import org.aaa.billingandcollections.dto.CreatePaymentResponse;
import org.aaa.billingandcollections.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CreatePaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }
}