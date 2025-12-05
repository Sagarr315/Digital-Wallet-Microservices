package com.digitalwallet.transactionservice.repository;

import com.digitalwallet.transactionservice.entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger, Long> {
    List<Ledger> findByUserIdOrderByTimestampDesc(Long userId);

    List<Ledger> findByTxnId(Long txnId);
}