package com.sparta.camp.java.FinalProject.domain.product.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSearchRequest {

  Long categoryId;

  Integer minPrice;

  Integer maxPrice;

  String sortType;

  String sortDirection;

  String keywordType;

  String keyword;

}
