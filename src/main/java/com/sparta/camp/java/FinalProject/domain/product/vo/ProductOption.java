package com.sparta.camp.java.FinalProject.domain.product.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductOption {

  @NotNull
  @Valid
  List<ColorOption> colors;

}
