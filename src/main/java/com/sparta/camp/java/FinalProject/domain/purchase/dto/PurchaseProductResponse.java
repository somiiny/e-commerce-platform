package com.sparta.camp.java.FinalProject.domain.purchase.dto;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseProductResponse {

  Long id;

  Long purchaseId;

  Long productId;

  String productName;

  String color;

  String size;

  Integer quantity;

  BigDecimal priceAtPurchase;

}
