package com.digitalwallet.paymentservice.dto;

public class ValidationResponse {
    private Boolean approved;
    private String message;

    public ValidationResponse() {
    }

    public ValidationResponse(Boolean approved, String message) {
        this.approved = approved;
        this.message = message;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}