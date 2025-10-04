package com.sparta.camp.java.FinalProject.common.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum PurchaseStatus {

  ORDER_PLACED("주문 접수"),
  PAYMENT_COMPLETED("결제 완료"),
  SHIPPING_PENDING("배송 준비 중"),
  IN_TRANSIT("배송 중"),
  DELIVERED("배송 완료");

  final String label;

}
