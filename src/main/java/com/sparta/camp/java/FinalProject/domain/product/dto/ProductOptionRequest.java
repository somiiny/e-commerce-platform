package com.sparta.camp.java.FinalProject.domain.product.dto;

import com.sparta.camp.java.FinalProject.common.enums.ColorType;
import com.sparta.camp.java.FinalProject.common.enums.SizeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductOptionRequest {

  @NotNull
  ColorType color;

  @NotNull
  SizeType size;

  @NotNull
  @PositiveOrZero
  Integer stock;

}
