package com.digitalwallet.authservice.dto;

public class ValidateResponse {
    private boolean valid;
    private Long userId;
    private String email;

    // Constructors
    public ValidateResponse() {
    }

    public ValidateResponse(boolean valid, Long userId, String email) {
        this.valid = valid;
        this.userId = userId;
        this.email = email;
    }

    // Getters and Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}