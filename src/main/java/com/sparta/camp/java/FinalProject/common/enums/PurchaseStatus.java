package com.sparta.camp.java.FinalProject.common.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum PurchaseStatus {

  PURCHASE_CREATED,
  PURCHASE_CANCELED,
  PURCHASE_PAID,
  PURCHASE_FULFILLING,
  PURCHASE_COMPLETED,
  PARTIALLY_REFUNDED,
  REFUNDED;

}
