package com.digitalwallet.walletservice.controller;

import com.digitalwallet.walletservice.dto.BalanceResponse;
import com.digitalwallet.walletservice.dto.LinkBankRequest;
import com.digitalwallet.walletservice.dto.WalletRequest;
import com.digitalwallet.walletservice.entity.Wallet;
import com.digitalwallet.walletservice.repository.WalletRepository;
import com.digitalwallet.walletservice.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @PostMapping("/create")
    public ResponseEntity<String> createWallet(
            @RequestBody WalletRequest request,
            @RequestAttribute("authenticatedUserId") Long userId) {
        try {
            return ResponseEntity.ok(walletService.createWallet(request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestParam Long userId,
                                        @RequestAttribute Long authenticatedUserId) {
        try {
            BalanceResponse response = walletService.getBalance(userId, authenticatedUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/link-bank")
    public ResponseEntity<String> linkBankAccount(@RequestParam Long userId,
                                                  @RequestBody LinkBankRequest request,
                                                  @RequestAttribute Long authenticatedUserId) {
        try {
            String result = walletService.linkBankAccount(userId, request, authenticatedUserId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // INTERNAL: For Transaction-Service ONLY
    @PostMapping("/internal/{userId}/deduct")
    public ResponseEntity<String> deductBalanceInternal(
            @PathVariable Long userId,
            @RequestBody Map<String, BigDecimal> request) {

        BigDecimal amount = request.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);
        if (walletOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Wallet not found");
        }

        Wallet wallet = walletOpt.get();
        if (wallet.getBalance().compareTo(amount) < 0) {
            return ResponseEntity.badRequest().body("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        return ResponseEntity.ok("Balance deducted successfully");
    }

    // INTERNAL: For Transaction-Service ONLY
    @PostMapping("/internal/{userId}/add")
    public ResponseEntity<String> addBalanceInternal(
            @PathVariable Long userId,
            @RequestBody Map<String, BigDecimal> request) {

        BigDecimal amount = request.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);
        if (walletOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Wallet not found");
        }

        Wallet wallet = walletOpt.get();
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        return ResponseEntity.ok("Balance added successfully");
    }

    @GetMapping("/internal/balance")
    public ResponseEntity<BigDecimal> getBalanceInternal(@RequestParam Long userId) {
        try {
            Optional<Wallet> wallet = walletRepository.findByUserId(userId);
            if (wallet.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            BigDecimal balance = wallet.get().getBalance();
            return ResponseEntity.ok(balance);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/internal/status/{userId}")
    public ResponseEntity<String> getWalletStatus(@PathVariable Long userId) {
        try {
            Optional<Wallet> wallet = walletRepository.findByUserId(userId);
            if (wallet.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT_FOUND");
            }

            String status = wallet.get().getStatus();
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR");
        }
    }
}