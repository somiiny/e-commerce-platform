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

  public boolean canTransitionTo(PurchaseStatus target) {
    return switch (this) {
      case PURCHASE_CREATED ->
          target == PURCHASE_PAID || target == PURCHASE_CANCELED;

      case PURCHASE_PAID ->
          target == PURCHASE_FULFILLING || target == PURCHASE_CANCELED;

      case PURCHASE_FULFILLING ->
          target == PURCHASE_COMPLETED || target == PARTIALLY_REFUNDED;

      case PURCHASE_COMPLETED ->
          target == PARTIALLY_REFUNDED || target == REFUNDED;

      case PARTIALLY_REFUNDED ->
          target == REFUNDED;

      case PURCHASE_CANCELED, REFUNDED ->
          false;
    };
  }

}
