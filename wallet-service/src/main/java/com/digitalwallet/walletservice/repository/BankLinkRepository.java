package com.digitalwallet.walletservice.repository;

import com.digitalwallet.walletservice.entity.LinkedBank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankLinkRepository extends JpaRepository<LinkedBank, Long> {
    List<LinkedBank> findByUserId(Long userId);

    Optional<LinkedBank> findByUserIdAndBankName(Long userId, String bankName);

    boolean existsByUserIdAndAccountNumber(Long userId, String accountNumber);
}