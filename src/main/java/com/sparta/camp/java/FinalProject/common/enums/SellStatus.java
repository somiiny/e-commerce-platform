package com.sparta.camp.java.FinalProject.common.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum SellStatus {
  ON_SALE("판매 중"),
  OUT_OF_STOCK("일시 품절"),
  SOLD_OUT("품절"),
  DISCONTINUED("판매 중지");

  final String label;
}
