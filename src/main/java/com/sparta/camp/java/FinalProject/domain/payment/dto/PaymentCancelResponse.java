package com.sparta.camp.java.FinalProject.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.camp.java.FinalProject.common.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCancelResponse {

  String paymentKey;

  @JsonProperty("orderId")
  Long purchaseId;

  @JsonProperty("orderName")
  String purchaseName;

  String method;

  @JsonProperty("totalAmount")
  BigDecimal amount;

  PaymentStatus status;

  boolean isPartialCancelable;

  String cardType;

  String cardNumber;

  String approveNo;

  List<CancelDetailsDto> cancels;
}
