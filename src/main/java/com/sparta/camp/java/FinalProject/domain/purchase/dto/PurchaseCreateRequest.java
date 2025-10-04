package com.sparta.camp.java.FinalProject.domain.purchase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseCreateRequest {

  @NotNull
  List<Long> cartProductIds;

  @NotBlank
  String receiverName;

  @NotBlank
  String zipCode;

  @NotBlank
  String shippingAddress;

  String shippingDetailAddress;

  @NotBlank
  String phoneNumber;
}
