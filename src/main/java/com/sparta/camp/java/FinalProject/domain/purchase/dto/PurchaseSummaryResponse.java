package com.sparta.camp.java.FinalProject.domain.purchase.dto;

import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseSummaryResponse {

  Long id;

  String purchaseNo;

  BigDecimal totalPrice;

  PurchaseStatus status;

  LocalDateTime createdAt;

  public PurchaseSummaryResponse(Long id, String purchaseNo, BigDecimal totalPrice,
      PurchaseStatus status, LocalDateTime createdAt) {
    this.id = id;
    this.purchaseNo = purchaseNo;
    this.totalPrice = totalPrice;
    this.status = status;
    this.createdAt = createdAt;
  }
}
