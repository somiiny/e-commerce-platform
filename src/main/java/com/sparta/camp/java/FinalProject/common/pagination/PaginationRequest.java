package com.sparta.camp.java.FinalProject.common.pagination;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaginationRequest {

  @Positive
  Integer page;

  @Positive
  Integer size;

}
