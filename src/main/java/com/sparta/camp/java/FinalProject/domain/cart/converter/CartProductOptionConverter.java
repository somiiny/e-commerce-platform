package com.sparta.camp.java.FinalProject.domain.cart.converter;

import com.sparta.camp.java.FinalProject.common.converter.JsonAttributeConverter;
import com.sparta.camp.java.FinalProject.domain.cart.vo.CartProductOption;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CartProductOptionConverter extends JsonAttributeConverter<CartProductOption> {

  public CartProductOptionConverter() {
    super(CartProductOption.class);
  }
}
