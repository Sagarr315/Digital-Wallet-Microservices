package com.digitalwallet.walletservice.dto;

public class LinkBankRequest {
    private String bankName;
    private String accountNumber;

    // Constructors
    public LinkBankRequest() {
    }

    public LinkBankRequest(String bankName, String accountNumber) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }

    // Getters and Setters
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}