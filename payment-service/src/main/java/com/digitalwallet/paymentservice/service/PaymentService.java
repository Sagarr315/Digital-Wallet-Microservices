package com.digitalwallet.paymentservice.service;

import com.digitalwallet.paymentservice.dto.PaymentRequest;
import com.digitalwallet.paymentservice.dto.ValidationResponse;
import com.digitalwallet.paymentservice.dto.ProcessPaymentResponse;
import com.digitalwallet.paymentservice.entity.PaymentValidationLog;
import com.digitalwallet.paymentservice.repository.PaymentValidationLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PaymentValidationLogRepository logRepository;

    @Autowired

    private HttpServletRequest httpServletRequest;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final BigDecimal DAILY_LIMIT = new BigDecimal("50000.00");

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1.00");

    public ValidationResponse validatePayment(PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(MIN_AMOUNT) < 0) {
            saveLog(request.getUserId(), request.getAmount(), false, "Amount must be at least ₹1");
            return new ValidationResponse(false, "Amount must be at least ₹1");
        }
        if (request.getAmount().compareTo(DAILY_LIMIT) > 0) {
            saveLog(request.getUserId(), request.getAmount(), false, "Amount exceeds daily limit of ₹50,000");
            return new ValidationResponse(false, "Amount exceeds daily limit of ₹50,000");
        }

        BigDecimal senderBalance = getWalletBalance(request.getUserId());
        if (senderBalance == null) {
            saveLog(request.getUserId(), request.getAmount(), false, "Sender wallet not found");
            return new ValidationResponse(false, "Sender wallet not found");
        }
        if (senderBalance.compareTo(request.getAmount()) < 0) {
            saveLog(request.getUserId(), request.getAmount(), false, "Insufficient balance");
            return new ValidationResponse(false, "Insufficient balance");
        }

        BigDecimal receiverBalance = getWalletBalance(request.getReceiverId());
        if (receiverBalance == null) {
            saveLog(request.getUserId(), request.getAmount(), false, "Receiver wallet not found");
            return new ValidationResponse(false, "Receiver wallet not found");
        }

        saveLog(request.getUserId(), request.getAmount(), true, "Payment validated successfully");
        return new ValidationResponse(true, "Payment validated successfully");
    }

    public ProcessPaymentResponse processPayment(PaymentRequest request) {
        ValidationResponse validation = validatePayment(request);
        if (!validation.getApproved()) {
            return new ProcessPaymentResponse(false, "Cannot process: " + validation.getMessage());
        }
        String referenceId = "PAYREF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        saveLog(request.getUserId(), request.getAmount(), true, "Payment processed with ref: " + referenceId);
        return new ProcessPaymentResponse(true, referenceId);
    }

    private BigDecimal getWalletBalance(Long userId) {
        try {
            String url = "http://WALLET-SERVICE/wallet/internal/balance?userId=" + userId;
            String authHeader = httpServletRequest.getHeader("Authorization");
            if (authHeader == null) return null;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return new BigDecimal(response.getBody());
            }
        } catch (Exception e) {
        }
        return null;
    }

    private void saveLog(Long userId, BigDecimal amount, Boolean validated, String reason) {
        PaymentValidationLog log = new PaymentValidationLog(userId, amount, validated, reason);
        logRepository.save(log);
    }
}