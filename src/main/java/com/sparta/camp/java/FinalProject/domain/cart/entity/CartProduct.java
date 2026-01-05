package com.sparta.camp.java.FinalProject.domain.cart.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductOption;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
public class CartProduct {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name= "cart_id", nullable = false)
  @JsonBackReference
  Cart cart;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @JsonBackReference
  Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_option_id", nullable = false)
  @JsonBackReference
  ProductOption option;

  @Column(nullable = false)
  Integer quantity;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  LocalDateTime createdAt;

  @Column
  @UpdateTimestamp
  LocalDateTime updatedAt;

  @Column
  LocalDateTime deletedAt;

  @Builder
  public CartProduct(Cart cart, Product product, ProductOption option, Integer quantity) {
    this.cart = cart;
    this.product = product;
    this.option = option;
    this.quantity = quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public void setDeletedAt(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

  public void increaseQuantity(Integer quantity) {
    this.quantity += quantity;
  }
}
