package com.sparta.camp.java.FinalProject.domain.product.mapper;

import com.sparta.camp.java.FinalProject.domain.product.dto.ProductOptionResponse;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductOptionMapper {

  @Mapping(source = "product.id", target = "productId")
  ProductOptionResponse toResponse(ProductOption productOption);

}
