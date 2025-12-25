package com.sparta.camp.java.FinalProject.domain.product.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SizeOption {

  @NotBlank
  String sizeName;

  @NotNull
  int stock;

  public void setStock(int stock) {
    this.stock = stock;
  }
}
