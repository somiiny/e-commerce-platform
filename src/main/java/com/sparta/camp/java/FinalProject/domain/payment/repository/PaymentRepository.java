package com.sparta.camp.java.FinalProject.domain.payment.repository;

import com.sparta.camp.java.FinalProject.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
