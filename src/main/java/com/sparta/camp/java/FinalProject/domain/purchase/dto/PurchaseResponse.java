package com.sparta.camp.java.FinalProject.domain.purchase.dto;

import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseResponse {

  Long id;

  String purchaseNo;

  BigDecimal totalPrice;

  PurchaseStatus status;

  List<PurchaseProductResponse> purchaseProductList;

  String receiverName;

  String zipCode;

  String shippingAddress;

  String shippingDetailAddress;

  String phoneNumber;
}
