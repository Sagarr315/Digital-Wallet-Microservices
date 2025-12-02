package com.digitalwallet.paymentservice.dto;

import java.math.BigDecimal;

public class PaymentRequest {
    private Long userId;
    private Long receiverId;
    private BigDecimal amount;

    public PaymentRequest() {
    }

    public PaymentRequest(Long userId, Long receiverId, BigDecimal amount) {
        this.userId = userId;
        this.receiverId = receiverId;
        this.amount = amount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}