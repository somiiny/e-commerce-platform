package com.sparta.camp.java.FinalProject.domain.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CancelDetailsDto {

  String cancelReason;

  BigDecimal cancelAmount;

  LocalDateTime canceledAt;

  String cancelStatus;

}
