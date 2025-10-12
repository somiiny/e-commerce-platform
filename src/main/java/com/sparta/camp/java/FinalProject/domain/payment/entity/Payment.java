package com.sparta.camp.java.FinalProject.domain.payment.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sparta.camp.java.FinalProject.common.enums.PaymentStatus;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "purchase_id", nullable = false)
  @JsonBackReference
  Purchase purchase;

  @Column(nullable = false)
  String transactionId;

  @Column(nullable = false, length = 50)
  String method;

  @Column(nullable = false)
  BigDecimal amount;

  @Column
  BigDecimal refundedAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  PaymentStatus status;

  @Column
  LocalDateTime paidAt;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  LocalDateTime createdAt;

  @Column
  @UpdateTimestamp
  LocalDateTime updatedAt;

  @Builder
  public Payment(Purchase purchase, String transactionId, String method, BigDecimal amount,
      LocalDateTime paidAt, PaymentStatus status) {
    this.purchase = purchase;
    this.transactionId = transactionId;
    this.method = method;
    this.amount = amount;
    this.paidAt = paidAt;
    this.status = status;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }
}
