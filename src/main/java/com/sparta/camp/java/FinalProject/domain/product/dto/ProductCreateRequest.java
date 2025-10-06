package com.sparta.camp.java.FinalProject.domain.product.dto;

import com.sparta.camp.java.FinalProject.common.enums.SellStatus;
import com.sparta.camp.java.FinalProject.domain.product.vo.ProductOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreateRequest {

  @NotNull
  Long categoryId;

  @NotBlank
  String name;

  @NotNull
  @PositiveOrZero
  BigDecimal price;

  @NotNull
  @PositiveOrZero
  Integer stock;

  @NotBlank
  String description;

  ProductOption options;

  SellStatus sellStatus;

  @NotNull
  List<MultipartFile> imageList;

}
