package com.sparta.camp.java.FinalProject.domain.payment.dto;

import com.sparta.camp.java.FinalProject.common.enums.CancelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCancelRequest {

  @NotBlank
  String paymentKey;

  @NotNull
  Long purchaseId;

  @NotNull
  CancelType cancelType;

  @NotBlank
  String cancelReason;

  BigDecimal amount;

  List<CancelProductDto> cancelProducts;
}
