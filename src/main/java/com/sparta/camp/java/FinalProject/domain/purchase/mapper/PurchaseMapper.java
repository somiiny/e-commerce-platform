package com.sparta.camp.java.FinalProject.domain.purchase.mapper;

import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseProductResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.PurchaseProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchaseMapper {

  @Mapping(source = "purchaseStatus", target = "status")
  PurchaseResponse toResponse(Purchase purchase);

  @Mapping(source = "purchase.id", target = "purchaseId")
  @Mapping(source = "product.id", target = "productId")
  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "purchasedOption.color", target = "color")
  @Mapping(source = "purchasedOption.size", target = "size")
  PurchaseProductResponse toResponse(PurchaseProduct purchaseProduct);

}
