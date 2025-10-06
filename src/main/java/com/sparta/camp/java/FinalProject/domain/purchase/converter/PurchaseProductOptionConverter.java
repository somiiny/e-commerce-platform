package com.sparta.camp.java.FinalProject.domain.purchase.converter;

import com.sparta.camp.java.FinalProject.common.converter.JsonAttributeConverter;
import com.sparta.camp.java.FinalProject.domain.purchase.vo.PurchaseProductOption;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PurchaseProductOptionConverter extends JsonAttributeConverter<PurchaseProductOption> {
  public PurchaseProductOptionConverter() {
    super(PurchaseProductOption.class);
  }
}
