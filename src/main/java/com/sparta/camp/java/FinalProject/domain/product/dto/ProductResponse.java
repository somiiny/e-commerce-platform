package com.sparta.camp.java.FinalProject.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.camp.java.FinalProject.common.enums.SellStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

  Long id;

  Long categoryId;

  String name;

  BigDecimal price;

  String description;

  SellStatus sellStatus;

  @Setter
  List<ProductOptionResponse> productOptions;

  @Setter
  List<ProductImageResponse> productImages;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  LocalDateTime createdAt;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  LocalDateTime updatedAt;

  @Builder
  public ProductResponse(Long id, Long categoryId, String name, BigDecimal price,
      String description, SellStatus sellStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.categoryId = categoryId;
    this.name = name;
    this.price = price;
    this.description = description;
    this.sellStatus = sellStatus;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  @Builder
  public ProductResponse(Long id, Long categoryId, String name, BigDecimal price,
      String description,
      SellStatus sellStatus, List<ProductOptionResponse> productOptions,
      List<ProductImageResponse> productImages) {
    this.id = id;
    this.categoryId = categoryId;
    this.name = name;
    this.price = price;
    this.description = description;
    this.sellStatus = sellStatus;
    this.productOptions = productOptions;
    this.productImages = productImages;
  }
}
