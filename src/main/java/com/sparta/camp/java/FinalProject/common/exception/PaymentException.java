package com.sparta.camp.java.FinalProject.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentException extends RuntimeException {

  String code;
  String message;

  public PaymentException(String code, String message) {
    this.code = code;
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }

}
