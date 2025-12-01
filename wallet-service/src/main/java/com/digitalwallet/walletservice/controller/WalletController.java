package com.digitalwallet.walletservice.controller;

import com.digitalwallet.walletservice.dto.BalanceResponse;
import com.digitalwallet.walletservice.dto.LinkBankRequest;
import com.digitalwallet.walletservice.dto.WalletRequest;
import com.digitalwallet.walletservice.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<String> createWallet(
            @RequestBody WalletRequest request,
            @RequestAttribute("authenticatedUserId") Long userId
    ) {
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
}