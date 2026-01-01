package com.sparta.camp.java.FinalProject.domain.product.dto;

import com.sparta.camp.java.FinalProject.common.enums.ColorType;
import com.sparta.camp.java.FinalProject.common.enums.SizeType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductOptionResponse {

  Long id;

  Long productId;

  ColorType color;

  SizeType size;

  Integer stock;

}
