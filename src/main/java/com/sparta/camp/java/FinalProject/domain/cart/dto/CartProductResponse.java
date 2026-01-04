package com.sparta.camp.java.FinalProject.domain.cart.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CartProductResponse {

  Long id;

  Long productId;

  Long productOptionId;

  Integer quantity;

}
