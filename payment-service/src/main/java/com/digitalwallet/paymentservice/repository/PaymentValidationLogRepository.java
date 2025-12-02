package com.digitalwallet.paymentservice.repository;

import com.digitalwallet.paymentservice.entity.PaymentValidationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentValidationLogRepository extends JpaRepository<PaymentValidationLog, Long> {
}