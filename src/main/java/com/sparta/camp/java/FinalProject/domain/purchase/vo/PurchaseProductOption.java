package com.sparta.camp.java.FinalProject.domain.purchase.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseProductOption {

  String color;

  String size;

}
