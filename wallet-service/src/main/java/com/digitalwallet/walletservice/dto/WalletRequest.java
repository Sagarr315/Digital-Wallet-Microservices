package com.digitalwallet.walletservice.dto;

public class WalletRequest {
    private Long userId;

    // Constructors
    public WalletRequest() {
    }

    public WalletRequest(Long userId) {
        this.userId = userId;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}