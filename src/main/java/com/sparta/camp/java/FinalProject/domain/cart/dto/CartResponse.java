package com.sparta.camp.java.FinalProject.domain.cart.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CartResponse {

  Long cartId;

  List<CartProductResponse> cartProductList;

  BigDecimal totalPrice;

}
