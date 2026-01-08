package com.sparta.camp.java.FinalProject.domain.purchase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DirectPurchaseCreateRequest {

  @NotNull
  Long productId;

  @NotNull
  Long productOptionId;

  @NotNull
  @Min(1)
  Integer quantity;

  @NotBlank
  String receiverName;

  @NotBlank
  @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리여야 합니다.")
  String zipCode;

  @NotBlank
  String shippingAddress;

  String shippingDetailAddress;

  @NotBlank
  @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "유효하지 않은 전화번호 입니다.")
  String phoneNumber;

}
