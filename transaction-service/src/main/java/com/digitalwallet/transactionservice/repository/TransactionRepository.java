package com.digitalwallet.transactionservice.repository;

import com.digitalwallet.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySenderId(Long senderId);

    List<Transaction> findBySenderIdOrReceiverIdOrderByTimestampDesc(Long senderId, Long receiverId);

    List<Transaction> findBySenderIdOrderByTimestampDesc(Long senderId);

    List<Transaction> findByReceiverIdOrderByTimestampDesc(Long receiverId);

}