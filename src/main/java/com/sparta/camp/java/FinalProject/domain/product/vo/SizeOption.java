package com.sparta.camp.java.FinalProject.domain.product.vo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SizeOption {

  String sizeName;

  int stock;

  public void setStock(int stock) {
    this.stock = stock;
  }
}
