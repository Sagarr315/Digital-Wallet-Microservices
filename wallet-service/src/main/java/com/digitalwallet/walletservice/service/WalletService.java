package com.digitalwallet.walletservice.service;

import com.digitalwallet.walletservice.dto.BalanceResponse;
import com.digitalwallet.walletservice.dto.LinkBankRequest;
import com.digitalwallet.walletservice.dto.WalletRequest;
import com.digitalwallet.walletservice.entity.LinkedBank;
import com.digitalwallet.walletservice.entity.Wallet;
import com.digitalwallet.walletservice.repository.BankLinkRepository;
import com.digitalwallet.walletservice.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private BankLinkRepository bankLinkRepository;

    public String createWallet(WalletRequest request, Long authenticatedUserId) {
        if (!request.getUserId().equals(authenticatedUserId)) {
            throw new RuntimeException("Unauthorized: Can only create your own wallet");
        }

        if (walletRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("Wallet already exists for this user");
        }

        Wallet wallet = new Wallet(request.getUserId());
        walletRepository.save(wallet);

        return "Wallet created successfully";
    }

    public BalanceResponse getBalance(Long userId, Long authenticatedUserId) {
        if (!userId.equals(authenticatedUserId)) {
            throw new RuntimeException("Unauthorized: Can only check your own balance");
        }

        Optional<Wallet> wallet = walletRepository.findByUserId(userId);
        if (wallet.isEmpty()) {
            throw new RuntimeException("Wallet not found");
        }

        return new BalanceResponse(userId, wallet.get().getBalance());
    }

    public String linkBankAccount(Long userId, LinkBankRequest request, Long authenticatedUserId) {
        if (!userId.equals(authenticatedUserId)) {
            throw new RuntimeException("Unauthorized: Can only link bank to your own wallet");
        }

        if (bankLinkRepository.existsByUserIdAndAccountNumber(userId, request.getAccountNumber())) {
            throw new RuntimeException("Bank account already linked");
        }

        LinkedBank linkedBank = new LinkedBank(userId, request.getBankName(), request.getAccountNumber());
        bankLinkRepository.save(linkedBank);

        return "Bank linked successfully";
    }
}