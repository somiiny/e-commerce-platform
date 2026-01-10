package com.sparta.camp.java.FinalProject.domain.purchase.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.domain.payment.entity.Payment;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.BatchSize;
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
public class Purchase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonBackReference
  User user;

  @Column(nullable = false, unique = true)
  String purchaseNo;

  @Column(nullable = false)
  BigDecimal totalPrice;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  PurchaseStatus purchaseStatus;

  @Column
  BigDecimal refundedAmount;

  @Column(nullable = false)
  String receiverName;

  @Column(nullable = false)
  String zipCode;

  @Column(nullable = false)
  String shippingAddress;

  @Column
  String shippingDetailAddress;

  @Column(nullable = false)
  String phoneNumber;

  @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @BatchSize(size = 50)
  List<PurchaseProduct> purchaseProductList = new ArrayList<>();

  @OneToMany(mappedBy = "purchase", fetch = FetchType.LAZY)
  List<Payment> paymentList = new ArrayList<>();

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  LocalDateTime createdAt;

  @Column
  @UpdateTimestamp
  LocalDateTime updatedAt;

  @Builder
  public Purchase(User user, String purchaseNo, BigDecimal totalPrice,
      PurchaseStatus purchaseStatus, BigDecimal refundedAmount,
      String receiverName, String phoneNumber, String zipCode, String shippingAddress,
      String shippingDetailAddress) {
    this.user = user;
    this.purchaseNo = purchaseNo;
    this.totalPrice = totalPrice;
    this.purchaseStatus = purchaseStatus;
    this.refundedAmount = refundedAmount;
    this.receiverName = receiverName;
    this.phoneNumber = phoneNumber;
    this.zipCode = zipCode;
    this.shippingAddress = shippingAddress;
    this.shippingDetailAddress = shippingDetailAddress;
  }

  public void addPurchaseProduct(PurchaseProduct purchaseProduct) {
    purchaseProductList.add(purchaseProduct);
    purchaseProduct.setPurchase(this);
  }

  public void setPurchaseStatus(PurchaseStatus purchaseStatus) {
    this.purchaseStatus = purchaseStatus;
  }

  public void setRefundedAmount(BigDecimal refundedAmount) {
    this.refundedAmount = refundedAmount;
  }
}
