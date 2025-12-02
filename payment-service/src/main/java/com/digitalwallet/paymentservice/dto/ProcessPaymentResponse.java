package com.digitalwallet.paymentservice.dto;

public class ProcessPaymentResponse {
    private Boolean processed;
    private String referenceId;

    public ProcessPaymentResponse() {
    }

    public ProcessPaymentResponse(Boolean processed, String referenceId) {
        this.processed = processed;
        this.referenceId = referenceId;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}