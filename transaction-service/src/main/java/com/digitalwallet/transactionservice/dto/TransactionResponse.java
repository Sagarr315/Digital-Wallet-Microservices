package com.digitalwallet.transactionservice.dto;

public class TransactionResponse {
    private Boolean success;
    private Long transactionId;
    private String referenceId;
    private String message;

    public TransactionResponse() {
    }

    public TransactionResponse(Boolean success, Long transactionId,
                               String referenceId, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
