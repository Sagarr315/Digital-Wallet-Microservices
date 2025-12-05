package com.digitalwallet.transactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionHistoryResponse {
    private Long txnId;
    private String type; // "DEBIT" or "CREDIT"
    private BigDecimal amount;
    private Long counterpartyId; // other user in transaction
    private LocalDateTime date;

    public TransactionHistoryResponse() {
    }

    public TransactionHistoryResponse(Long txnId, String type, BigDecimal amount,
                                      Long counterpartyId, LocalDateTime date) {
        this.txnId = txnId;
        this.type = type;
        this.amount = amount;
        this.counterpartyId = counterpartyId;
        this.date = date;
    }

    public Long getTxnId() {
        return txnId;
    }

    public void setTxnId(Long txnId) {
        this.txnId = txnId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getCounterpartyId() {
        return counterpartyId;
    }

    public void setCounterpartyId(Long counterpartyId) {
        this.counterpartyId = counterpartyId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}