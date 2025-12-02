package com.digitalwallet.paymentservice.controller;

import com.digitalwallet.paymentservice.dto.PaymentRequest;
import com.digitalwallet.paymentservice.dto.ValidationResponse;
import com.digitalwallet.paymentservice.dto.ProcessPaymentResponse;
import com.digitalwallet.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validatePayment(
            @RequestBody PaymentRequest request,
            @RequestAttribute("authenticatedUserId") Long authenticatedUserId) {
        ValidationResponse response = paymentService.validatePayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process")
    public ResponseEntity<ProcessPaymentResponse> processPayment(
            @RequestBody PaymentRequest request,
            @RequestAttribute("authenticatedUserId") Long authenticatedUserId) {
        ProcessPaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }
}