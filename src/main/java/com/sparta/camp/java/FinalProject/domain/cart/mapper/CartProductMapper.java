package com.sparta.camp.java.FinalProject.domain.cart.mapper;

import com.sparta.camp.java.FinalProject.domain.cart.dto.CartProductResponse;
import com.sparta.camp.java.FinalProject.domain.cart.entity.CartProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartProductMapper {

  @Mapping(source = "product.id", target = "productId")
  @Mapping(source = "option.id", target = "productOptionId")
  CartProductResponse toResponse(CartProduct cartProduct);

}
