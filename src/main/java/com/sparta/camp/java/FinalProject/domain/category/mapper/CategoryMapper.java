package com.sparta.camp.java.FinalProject.domain.category.mapper;

import com.sparta.camp.java.FinalProject.domain.category.dto.CategoryResponse;
import com.sparta.camp.java.FinalProject.domain.category.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  @Mapping(source = "parent.id", target = "parentId")
  CategoryResponse toResponse(Category category);
}
