package com.digitalwallet.transactionservice.dto;

import java.math.BigDecimal;

public class SendMoneyRequest {
    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;

    public SendMoneyRequest() {
    }

    public SendMoneyRequest(Long senderId, Long receiverId, BigDecimal amount) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
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