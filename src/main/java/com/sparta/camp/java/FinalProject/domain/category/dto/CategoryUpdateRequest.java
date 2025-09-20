package com.sparta.camp.java.FinalProject.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryUpdateRequest {

  @NotBlank
  Long id;

  Long parentId;

  @NotBlank
  String name;

}
