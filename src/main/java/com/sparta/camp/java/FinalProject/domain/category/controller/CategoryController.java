package com.sparta.camp.java.FinalProject.domain.category.controller;

import com.sparta.camp.java.FinalProject.common.response.ApiResponse;
import com.sparta.camp.java.FinalProject.domain.category.dto.CategoryCreateRequest;
import com.sparta.camp.java.FinalProject.domain.category.dto.CategoryResponse;
import com.sparta.camp.java.FinalProject.domain.category.dto.CategoryUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.category.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping("/hierarchy")
  public ApiResponse<List<CategoryResponse>> getCategoryHierarchy(){
    return ApiResponse.success(categoryService.getCategoryHierarchy());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Long> createCategory(@Valid @RequestBody CategoryCreateRequest categoryCreateRequest) {
    return ApiResponse.success(categoryService.createCategory(categoryCreateRequest));
  }

  @PutMapping("/{categoryId}")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<CategoryResponse> updateCategory(@PathVariable("categoryId") Long id, @Valid @RequestBody CategoryUpdateRequest categoryUpdateRequest) {
    return ApiResponse.success(categoryService.updateCategory(id, categoryUpdateRequest));
  }

  @DeleteMapping("/{categoryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ApiResponse<Void> deleteCategory(@PathVariable("categoryId") Long id) {
    categoryService.deleteCategory(id);
    return ApiResponse.success();
  }

}
