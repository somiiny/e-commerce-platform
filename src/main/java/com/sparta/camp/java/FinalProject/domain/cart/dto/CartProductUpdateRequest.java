package com.sparta.camp.java.FinalProject.domain.cart.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartProductUpdateRequest {

  Integer quantity;

}
