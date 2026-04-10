package com.digitalwallet.transactionservice.repository;

import com.digitalwallet.transactionservice.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {
        Optional<IdempotencyRecord> findByKey(String key);
    }

