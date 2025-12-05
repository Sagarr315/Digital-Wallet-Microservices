package com.digitalwallet.transactionservice.service;

import com.digitalwallet.transactionservice.dto.SendMoneyRequest;
import com.digitalwallet.transactionservice.dto.TransactionResponse;
import com.digitalwallet.transactionservice.dto.TransactionHistoryResponse;
import com.digitalwallet.transactionservice.dto.TransactionDetailResponse;
import com.digitalwallet.transactionservice.entity.Transaction;
import com.digitalwallet.transactionservice.entity.Ledger;
import com.digitalwallet.transactionservice.repository.TransactionRepository;
import com.digitalwallet.transactionservice.repository.LedgerRepository;
import com.digitalwallet.transactionservice.kafka.PaymentEventProducer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private PaymentEventProducer paymentEventProducer;

    @Autowired
    private HttpServletRequest httpServletRequest;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public TransactionResponse sendMoney(SendMoneyRequest request) {
        try {
            if (!validatePaymentWithPaymentService(request)) {
                return new TransactionResponse(false, null, null, "Payment validation failed");
            }

            String referenceId = getPaymentReference(request);
            if (referenceId == null) {
                return new TransactionResponse(false, null, null, "Failed to get payment reference");
            }

            if (!updateWalletBalance(request.getSenderId(), request.getAmount(), "deduct")) {
                return new TransactionResponse(false, null, null, "Failed to deduct from sender wallet");
            }

            if (!updateWalletBalance(request.getReceiverId(), request.getAmount(), "add")) {
                updateWalletBalance(request.getSenderId(), request.getAmount(), "add");
                return new TransactionResponse(false, null, null, "Failed to add to receiver wallet");
            }

            Transaction transaction = new Transaction(
                    request.getSenderId(),
                    request.getReceiverId(),
                    request.getAmount(),
                    "SUCCESS",
                    referenceId
            );
            transaction = transactionRepository.save(transaction);

            createLedgerEntries(transaction);

            try {
                paymentEventProducer.sendPaymentEvent(
                        transaction.getId(),
                        request.getSenderId(),
                        request.getReceiverId(),
                        request.getAmount().toString(),
                        referenceId
                );
            } catch (Exception kafkaError) {
            }

            return new TransactionResponse(true, transaction.getId(),
                    referenceId, "Payment sent successfully");

        } catch (Exception e) {
            return new TransactionResponse(false, null, null,
                    "Transaction failed: " + e.getMessage());
        }
    }

    public List<TransactionHistoryResponse> getTransactionHistory(Long userId) {
        try {
            List<Transaction> allTransactions =
                    transactionRepository.findBySenderIdOrReceiverIdOrderByTimestampDesc(userId, userId);

            return allTransactions.stream()
                    .map(txn -> {
                        boolean isSender = txn.getSenderId().equals(userId);
                        return new TransactionHistoryResponse(
                                txn.getId(),
                                isSender ? "DEBIT" : "CREDIT",
                                txn.getAmount(),
                                isSender ? txn.getReceiverId() : txn.getSenderId(),
                                txn.getTimestamp()
                        );
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public TransactionDetailResponse getTransactionDetails(Long txnId) {
        try {
            Optional<Transaction> transactionOpt = transactionRepository.findById(txnId);
            if (transactionOpt.isEmpty()) {
                return null;
            }

            Transaction transaction = transactionOpt.get();

            return new TransactionDetailResponse(
                    transaction.getId(),
                    transaction.getSenderId(),
                    transaction.getReceiverId(),
                    transaction.getAmount(),
                    transaction.getStatus(),
                    transaction.getReferenceId(),
                    transaction.getTimestamp()
            );

        } catch (Exception e) {
            return null;
        }
    }

    private boolean validatePaymentWithPaymentService(SendMoneyRequest request) {
        try {
            String url = "http://PAYMENT-SERVICE/payment/validate";
            String authHeader = httpServletRequest.getHeader("Authorization");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("userId", request.getSenderId());
            body.put("receiverId", request.getReceiverId());
            body.put("amount", request.getAmount());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode node = objectMapper.readTree(response.getBody());
                return node.get("approved").asBoolean();
            }
        } catch (Exception e) {
        }
        return false;
    }

    private String getPaymentReference(SendMoneyRequest request) {
        try {
            String url = "http://PAYMENT-SERVICE/payment/process";
            String authHeader = httpServletRequest.getHeader("Authorization");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("userId", request.getSenderId());
            body.put("receiverId", request.getReceiverId());
            body.put("amount", request.getAmount());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode node = objectMapper.readTree(response.getBody());
                return node.get("referenceId").asText();
            }
        } catch (Exception e) {
        }
        return null;
    }

    private boolean updateWalletBalance(Long userId, BigDecimal amount, String operation) {
        try {
            String endpoint = operation.equals("deduct") ? "deduct" : "add";
            String url = "http://WALLET-SERVICE/wallet/internal/" + userId + "/" + endpoint;

            String authHeader = httpServletRequest.getHeader("Authorization");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("amount", amount);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class
            );

            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            return false;
        }
    }

    private void createLedgerEntries(Transaction transaction) {
        Ledger debitEntry = new Ledger(transaction.getId(), transaction.getSenderId(), "DEBIT", transaction.getAmount());
        ledgerRepository.save(debitEntry);

        Ledger creditEntry = new Ledger(transaction.getId(), transaction.getReceiverId(), "CREDIT", transaction.getAmount());
        ledgerRepository.save(creditEntry);
    }
}