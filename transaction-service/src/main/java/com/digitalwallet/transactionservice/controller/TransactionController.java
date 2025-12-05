package com.digitalwallet.transactionservice.controller;

import com.digitalwallet.transactionservice.dto.SendMoneyRequest;
import com.digitalwallet.transactionservice.dto.TransactionResponse;
import com.digitalwallet.transactionservice.dto.TransactionHistoryResponse;
import com.digitalwallet.transactionservice.repository.TransactionRepository;
import com.digitalwallet.transactionservice.dto.TransactionDetailResponse;
import com.digitalwallet.transactionservice.entity.Transaction;
import com.digitalwallet.transactionservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/send")
    public ResponseEntity<TransactionResponse> sendMoney(
            @RequestBody SendMoneyRequest request,
            @RequestAttribute("authenticatedUserId") Long authenticatedUserId) {

        if (!request.getSenderId().equals(authenticatedUserId)) {
            return ResponseEntity.badRequest().body(
                    new TransactionResponse(false, null, null,
                            "Unauthorized: Can only send money from your own account")
            );
        }

        TransactionResponse response = transactionService.sendMoney(request);

        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getTransactionHistory(
            @PathVariable Long userId,
            @RequestAttribute("authenticatedUserId") Long authenticatedUserId) {

        if (!userId.equals(authenticatedUserId)) {
            return ResponseEntity.badRequest()
                    .body("Unauthorized: Can only view your own transaction history");
        }

        List<TransactionHistoryResponse> history =
                transactionService.getTransactionHistory(userId);

        return ResponseEntity.ok(history);
    }

    @GetMapping("/{txnId}")
    public ResponseEntity<?> getTransactionDetails(
            @PathVariable Long txnId,
            @RequestAttribute("authenticatedUserId") Long authenticatedUserId) {

        Optional<Transaction> transactionOpt = transactionRepository.findById(txnId);

        if (transactionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Transaction transaction = transactionOpt.get();

        boolean isAuthorized = transaction.getSenderId().equals(authenticatedUserId) ||
                transaction.getReceiverId().equals(authenticatedUserId);

        if (!isAuthorized) {
            return ResponseEntity.notFound().build();
        }

        TransactionDetailResponse details =
                transactionService.getTransactionDetails(txnId);

        return ResponseEntity.ok(details);
    }
}