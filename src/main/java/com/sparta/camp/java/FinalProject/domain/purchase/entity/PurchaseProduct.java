package com.sparta.camp.java.FinalProject.domain.purchase.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseProductStatus;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductOption;
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
public class PurchaseProduct {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "purchase_id", nullable = false)
  @JsonBackReference
  Purchase purchase;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @JsonBackReference
  Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_option_id", nullable = false)
  @JsonBackReference
  ProductOption purchasedOption;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  PurchaseProductStatus status;

  @Column(nullable = false)
  Integer quantity;

  @Column
  Integer refundedQuantity;

  @Column(nullable = false)
  BigDecimal priceAtPurchase;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  LocalDateTime createdAt;

  @Column
  @UpdateTimestamp
  LocalDateTime updatedAt;

  @Builder
  public PurchaseProduct(Purchase purchase, Product product, ProductOption purchasedOption,
      PurchaseProductStatus status, Integer quantity, BigDecimal priceAtPurchase) {
    this.purchase = purchase;
    this.product = product;
    this.purchasedOption = purchasedOption;
    this.status = status;
    this.quantity = quantity;
    this.priceAtPurchase = priceAtPurchase;
  }

  public void setPurchase(Purchase purchase) {
    this.purchase = purchase;
  }

  public void setStatus(PurchaseProductStatus status) {
    this.status = status;
  }

  public Integer getRemainingQuantity() {
    Integer refundedQuantity = this.refundedQuantity == null ? 0 : this.refundedQuantity;
    return this.quantity - refundedQuantity;
  }

  public void addRefundedQuantity(Integer quantity) {
    this.refundedQuantity = this.refundedQuantity != null ? this.refundedQuantity + quantity : quantity;
  }
}
