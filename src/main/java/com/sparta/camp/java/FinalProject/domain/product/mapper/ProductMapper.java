package com.sparta.camp.java.FinalProject.domain.product.mapper;

import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  @Mapping(source = "category.id", target = "categoryId")
  ProductResponse toResponse(Product product);

}
