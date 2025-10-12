package com.sparta.camp.java.FinalProject.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentConfirmRequest {

  @NotBlank
  String paymentKey;

  @NotNull
  @JsonProperty("orderId")
  Long purchaseId;

  @NotNull
  BigDecimal amount;
}
