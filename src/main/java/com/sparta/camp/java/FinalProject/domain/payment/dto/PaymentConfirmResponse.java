package com.sparta.camp.java.FinalProject.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.camp.java.FinalProject.common.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentConfirmResponse {

  String paymentKey;

  @JsonProperty("orderId")
  String purchaseId;

  @JsonProperty("orderName")
  String purchaseName;

  String method;

  @JsonProperty("totalAmount")
  BigDecimal amount;

  PaymentStatus status;

  LocalDateTime requestedAt;

  LocalDateTime approvedAt;
}
