package com.sparta.camp.java.FinalProject.domain.product.dto;

import com.sparta.camp.java.FinalProject.common.enums.SellStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateRequest {

  @NotNull
  Long id;

  @NotNull
  Long categoryId;

  @NotBlank
  String name;

  @NotNull
  @PositiveOrZero
  BigDecimal price;

  @NotBlank
  String description;

  @NotNull
  SellStatus sellStatus;

  @NotEmpty
  @Valid
  List<ProductOptionRequest> options;

  @NotEmpty
  @Valid
  List<MultipartFile> images;

}
