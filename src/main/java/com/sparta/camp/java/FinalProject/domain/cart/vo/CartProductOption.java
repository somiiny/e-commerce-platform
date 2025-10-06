package com.sparta.camp.java.FinalProject.domain.cart.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartProductOption {

  String color;

  String size;

}
