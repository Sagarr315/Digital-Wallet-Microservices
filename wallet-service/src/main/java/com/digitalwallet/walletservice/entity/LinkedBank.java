package com.digitalwallet.walletservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "linked_bank")
public class LinkedBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String bankName;

    private String accountNumber;

    private LocalDateTime linkedAt;

    @PrePersist
    protected void onCreate() {
        linkedAt = LocalDateTime.now();
    }

    // Constructors
    public LinkedBank() {
    }

    public LinkedBank(Long userId, String bankName, String accountNumber) {
        this.userId = userId;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public LocalDateTime getLinkedAt() {
        return linkedAt;
    }

    public void setLinkedAt(LocalDateTime linkedAt) {
        this.linkedAt = linkedAt;
    }
}