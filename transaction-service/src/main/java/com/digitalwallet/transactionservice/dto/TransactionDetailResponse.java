package com.digitalwallet.transactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDetailResponse {
    private Long txnId;
    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;
    private String status;
    private String referenceId;
    private LocalDateTime timestamp;

    public TransactionDetailResponse(Long txnId, Long senderId, Long receiverId,
                                     BigDecimal amount, String status,
                                     String referenceId, LocalDateTime timestamp) {
        this.txnId = txnId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.status = status;
        this.referenceId = referenceId;
        this.timestamp = timestamp;
    }

    public TransactionDetailResponse() {
    }
    // Getters and Setters
    public Long getTxnId() {
        return txnId;
    }

    public void setTxnId(Long txnId) {
        this.txnId = txnId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}