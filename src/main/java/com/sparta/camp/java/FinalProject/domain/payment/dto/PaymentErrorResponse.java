package com.sparta.camp.java.FinalProject.domain.payment.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentErrorResponse {

  String message;

  String code;
}
