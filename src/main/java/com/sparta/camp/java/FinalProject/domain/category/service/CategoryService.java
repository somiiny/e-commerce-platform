package com.sparta.camp.java.FinalProject.domain.category.service;

import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.category.dto.CategoryCreateRequest;
import com.sparta.camp.java.FinalProject.domain.category.dto.CategoryResponse;
import com.sparta.camp.java.FinalProject.domain.category.dto.CategoryUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.category.entity.Category;
import com.sparta.camp.java.FinalProject.domain.category.mapper.CategoryMapper;
import com.sparta.camp.java.FinalProject.domain.category.repository.CategoryRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;
  private final ProductRepository productRepository;

  @Transactional(readOnly = true)
  public List<CategoryResponse> getCategoryHierarchy() {
    List<Category> categories = categoryRepository.findCategoryAll();

    Map<Long, CategoryResponse> categoryResponseMap = new HashMap<>();
    for (Category category : categories) {
      CategoryResponse response = CategoryResponse.builder()
          .id(category.getId())
          .name(category.getName())
          .children(new ArrayList<>())
          .build();
      categoryResponseMap.put(category.getId(), response);
    }

    List<CategoryResponse> rootCategories = new ArrayList<>();
    for (Category category : categories) {
      CategoryResponse categoryResponse = categoryResponseMap.get(category.getId());

      if (ObjectUtils.isEmpty(category.getParent())) {
        rootCategories.add(categoryResponse);
      } else {
        CategoryResponse parentResponse = categoryResponseMap.get(category.getParent().getId());
        if (parentResponse != null) {
          parentResponse.getChildren().add(categoryResponse);
        }
      }
    }

    return rootCategories;
  }

  @Transactional
  public Long createCategory(CategoryCreateRequest categoryCreateRequest) {

    Category parentCategory = null;
    if (categoryCreateRequest.getParentId() != null) {
      parentCategory = this.getCategoryById(categoryCreateRequest.getParentId());
    }

    Category newCategory = Category.builder()
        .parent(parentCategory)
        .name(categoryCreateRequest.getName())
        .build();

    categoryRepository.save(newCategory);

    return newCategory.getId();
  }

  @Transactional
  public CategoryResponse updateCategory(Long id, CategoryUpdateRequest categoryUpdateRequest) {

    Category category = this.getCategoryById(id);

    Category parentCategory = null;
    if (categoryUpdateRequest.getParentId() != null) {
      parentCategory = this.getCategoryById(categoryUpdateRequest.getParentId());
      this.validateParent(category, parentCategory);
    }

    category.setName(categoryUpdateRequest.getName());
    category.setParent(parentCategory);

    return categoryMapper.toResponse(category);
  }

  @Transactional
  public void deleteCategory(Long id) {
    Category category = this.getCategoryById(id);

    if (!categoryRepository.findCategoryByParentId(id).isEmpty()
        || productRepository.existsByCategoryId(id)) {
      throw new ServiceException(ServiceExceptionCode.NOT_DELETE_CATEGORY);
    }

    category.setDeletedAt(LocalDateTime.now());
  }

  private Category getCategoryById(Long id) {
    return categoryRepository.findCategoryById(id)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_CATEGORY));
  }

  private void validateParent(Category category, Category parentCategory) {
    Category current = parentCategory;
    while(current != null) {
      if (current.getId().equals(category.getId())) {
        throw new ServiceException(ServiceExceptionCode.NOT_ALLOWED_SELF_PARENT);
      }
      current = current.getParent();
    }
  }

}
