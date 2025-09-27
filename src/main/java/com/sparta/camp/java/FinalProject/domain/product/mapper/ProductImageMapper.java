package com.sparta.camp.java.FinalProject.domain.product.mapper;

import com.sparta.camp.java.FinalProject.domain.product.dto.ProductImageResponse;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

  @Mapping(source = "product.id", target = "productId")
  ProductImageResponse toResponse(ProductImage productImage);
}
