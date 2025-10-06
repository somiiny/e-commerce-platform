package com.sparta.camp.java.FinalProject.domain.product.converter;

import com.sparta.camp.java.FinalProject.common.converter.JsonAttributeConverter;
import com.sparta.camp.java.FinalProject.domain.product.vo.ProductOption;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductOptionConverter extends JsonAttributeConverter<ProductOption> {

  protected ProductOptionConverter() {
    super(ProductOption.class);
  }
}
