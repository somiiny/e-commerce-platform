package com.sparta.camp.java.FinalProject.domain.cart.dto;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CartProductResponse {

  Long id;

  Long cartId;

  Long productId;

  String productName;

  String color;

  String size;

  Integer quantity;

  BigDecimal price;

}
