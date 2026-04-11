package com.digitalwallet.transactionservice.service;

import com.digitalwallet.transactionservice.entity.IdempotencyRecord;
import com.digitalwallet.transactionservice.dto.SendMoneyRequest;
import com.digitalwallet.transactionservice.dto.TransactionResponse;
import com.digitalwallet.transactionservice.dto.TransactionHistoryResponse;
import com.digitalwallet.transactionservice.dto.TransactionDetailResponse;
import com.digitalwallet.transactionservice.entity.Transaction;
import com.digitalwallet.transactionservice.entity.Ledger;
import com.digitalwallet.transactionservice.repository.IdempotencyRepository;
import com.digitalwallet.transactionservice.repository.TransactionRepository;
import com.digitalwallet.transactionservice.repository.LedgerRepository;
import com.digitalwallet.transactionservice.kafka.PaymentEventProducer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public TransactionResponse sendMoney(SendMoneyRequest request, String idempotencyKey) {
        try {
            String cachedResponse = redisTemplate.opsForValue().get(idempotencyKey);

            if (cachedResponse != null) {
                try {
                    return objectMapper.readValue(cachedResponse, TransactionResponse.class);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize Redis response");
                }
            }

            Optional<IdempotencyRecord> existing =
                    idempotencyRepository.findByKey(idempotencyKey);

            if (existing.isPresent() && existing.get().getResponse() != null) {
                IdempotencyRecord record = existing.get();

                redisTemplate.opsForValue().set(
                        idempotencyKey,
                        record.getResponse(),
                        java.time.Duration.ofMinutes(10)
                );

                try {
                    return objectMapper.readValue(
                            record.getResponse(),
                            TransactionResponse.class
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize response");
                }
            }

            IdempotencyRecord record = new IdempotencyRecord();
            record.setKey(idempotencyKey);
            record.setStatus("PENDING");
            idempotencyRepository.save(record);

            if (!validatePaymentWithPaymentService(request)) {
                record.setStatus("FAILED");
                idempotencyRepository.save(record);
                return new TransactionResponse(false, null, null, "Payment validation failed");
            }

            String referenceId = getPaymentReference(request);
            if (referenceId == null) {
                record.setStatus("FAILED");
                idempotencyRepository.save(record);
                return new TransactionResponse(false, null, null, "Failed to get payment reference");
            }

            if (!updateWalletBalance(request.getSenderId(), request.getAmount(), "deduct")) {
                record.setStatus("FAILED");
                idempotencyRepository.save(record);
                return new TransactionResponse(false, null, null, "Failed to deduct from sender wallet");
            }

            if (!updateWalletBalance(request.getReceiverId(), request.getAmount(), "add")) {
                updateWalletBalance(request.getSenderId(), request.getAmount(), "add");
                record.setStatus("FAILED");
                idempotencyRepository.save(record);
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
                        request.getAmount(),
                        referenceId
                );
            } catch (Exception kafkaError) {
            }

            TransactionResponse response = new TransactionResponse(
                    true,
                    transaction.getId(),
                    referenceId,
                    "Payment sent successfully"
            );

            try {
                String responseJson = objectMapper.writeValueAsString(response);

                record.setStatus("SUCCESS");
                record.setResponse(responseJson);
                idempotencyRepository.save(record);

                redisTemplate.opsForValue().set(
                        idempotencyKey,
                        responseJson,
                        java.time.Duration.ofMinutes(10)
                );
            } catch (Exception e) {
            }

            return response;

        } catch (Exception e) {

            Optional<IdempotencyRecord> recordOpt =
                    idempotencyRepository.findByKey(idempotencyKey);

            if (recordOpt.isPresent()) {
                IdempotencyRecord record = recordOpt.get();
                record.setStatus("FAILED");
                idempotencyRepository.save(record);
            }

            return new TransactionResponse(false, null, null,
                    "Transaction failed: " + e.getMessage());
        }
    }

    public Page<TransactionHistoryResponse> getTransactionHistoryPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        Page<Transaction> transactionPage =
                transactionRepository.findBySenderIdOrReceiverIdOrderByTimestampDesc(userId, userId, pageable);

        return transactionPage.map(txn -> {
            boolean isSender = txn.getSenderId().equals(userId);
            return new TransactionHistoryResponse(
                    txn.getId(),
                    isSender ? "DEBIT" : "CREDIT",
                    txn.getAmount(),
                    isSender ? txn.getReceiverId() : txn.getSenderId(),
                    txn.getTimestamp()
            );
        });
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