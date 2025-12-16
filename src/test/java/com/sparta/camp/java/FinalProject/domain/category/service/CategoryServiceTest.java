package com.sparta.camp.java.FinalProject.domain.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.category.dto.CategoryCreateRequest;
import com.sparta.camp.java.FinalProject.domain.category.dto.CategoryResponse;
import com.sparta.camp.java.FinalProject.domain.category.dto.CategoryUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.category.entity.Category;
import com.sparta.camp.java.FinalProject.domain.category.mapper.CategoryMapper;
import com.sparta.camp.java.FinalProject.domain.category.repository.CategoryRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @InjectMocks
  private CategoryService categoryService;

  @Mock
  CategoryRepository categoryRepository;

  @Mock
  CategoryMapper categoryMapper;

  private Long categoryId = 1L;

  private CategoryCreateRequest createWithParent;
  private CategoryCreateRequest createWithoutParent;

  private CategoryUpdateRequest updateWithParent;
  private CategoryUpdateRequest updateWithoutParent;
  private CategoryUpdateRequest updateSelfParent;

  private Category parentCategory;
  private Category testCategory;
  private Category testCategory2;

  private List<Category> categoryList;
  private List<Category> childCategoryList;

  private CategoryResponse categoryResponse;
  private CategoryResponse categoryResponse2;

  @BeforeEach
  void setUp() {
    categoryList = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      categoryList.add(categoryWithId("category"+ (i+1), null));
    }

    for (int i = 0; i < 3; i++) {
      categoryList.add(categoryWithId("child_category"+ (i+1), categoryList.get(i)));
    }

    parentCategory = categoryWithId("parent", null);
    testCategory = categoryWithId("test", parentCategory);
    testCategory2 = categoryWithId("test2", null);

    childCategoryList = new ArrayList<>();
    childCategoryList.add(categoryWithId("child1", testCategory));
    childCategoryList.add(categoryWithId("child2", testCategory));

    createWithParent = requestCreate(1L, "create_test");
    createWithoutParent = requestCreate(null, "create_test2");

    updateWithParent = requestUpdate(100L, 2L, "update_test");
    updateWithoutParent = requestUpdate(200L, null, "update_test2");
    updateSelfParent = requestUpdate(300L, 300L, "update_test3");

    categoryResponse = CategoryResponse.builder()
        .parentId(updateWithParent.getParentId())
        .name(updateWithParent.getName())
        .build();

    categoryResponse2 = CategoryResponse.builder()
        .name(updateWithoutParent.getName())
        .build();
  }

  private Category categoryWithId(String name, Category parent) {
    Category category = Category.builder()
        .name(name)
        .parent(parent)
        .build();
    ReflectionTestUtils.setField(category, "id", categoryId++);
    return category;
  }

  private CategoryCreateRequest requestCreate(Long parentId, String name) {
    CategoryCreateRequest request = new CategoryCreateRequest();
    ReflectionTestUtils.setField(request, "parentId", parentId);
    ReflectionTestUtils.setField(request, "name", name);
    return request;
  }

  private CategoryUpdateRequest requestUpdate(Long id, Long parentId, String name) {
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    ReflectionTestUtils.setField(request, "id", id);
    ReflectionTestUtils.setField(request, "parentId", parentId);
    ReflectionTestUtils.setField(request, "name", name);
    return request;
  }

  @Test
  @DisplayName("카테고리 계층이 정상적으로 조회된다.")
  void getCategoryHierarchy_should_return_true() {
    when(categoryRepository.findCategoryAll())
        .thenReturn(categoryList);

    List<CategoryResponse> result = categoryService.getCategoryHierarchy();

    assertThat(result).isNotNull();
    verify(categoryRepository).findCategoryAll();
  }

  @Test
  @DisplayName("부모가 있는 카테고리가 정상적으로 생성된다.")
  void createCategory_should_return_true() {
    when(categoryRepository.findCategoryById(createWithParent.getParentId()))
        .thenReturn(Optional.of(parentCategory));
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> {
          Category arg = invocation.getArgument(0);
          ReflectionTestUtils.setField(arg, "id", 999L);
          return arg;
        });

    Long result = categoryService.createCategory(createWithParent);

    assertThat(result).isNotNull();
    verify(categoryRepository).findCategoryById(createWithParent.getParentId());
    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  @DisplayName("부모가 없는 카테고리가 정상적으로 생성된다.")
  void createCategory_should_return_true_when_parentCategory_is_null() {
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> {
          Category arg = invocation.getArgument(0);
          ReflectionTestUtils.setField(arg, "id", 999L);
          return arg;
        });

    Long result = categoryService.createCategory(createWithoutParent);

    assertThat(result).isNotNull();
    verify(categoryRepository, never()).findCategoryById(any());
    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  @DisplayName("부모가 있는 카테고리가 정상적으로 수정된다.")
  void updateCategory_should_return_true() {
    ReflectionTestUtils.setField(parentCategory, "id", 2L);

    when(categoryRepository.findCategoryById(updateWithParent.getId()))
      .thenReturn(Optional.of(testCategory));
    when(categoryRepository.findCategoryById(updateWithParent.getParentId()))
      .thenReturn(Optional.of(parentCategory));
    when(categoryMapper.toResponse(any(Category.class)))
      .thenReturn(categoryResponse);

    CategoryResponse result =
        categoryService.updateCategory(updateWithParent.getId(), updateWithParent);

    assertThat(result).isNotNull();
    assertThat(testCategory.getName()).isEqualTo(updateWithParent.getName());
    assertThat(testCategory.getParent().getId()).isEqualTo(updateWithParent.getParentId());

    verify(categoryRepository).findCategoryById(updateWithParent.getId());
    verify(categoryRepository).findCategoryById(updateWithParent.getParentId());
    verify(categoryMapper).toResponse(any(Category.class));
  }

  @Test
  @DisplayName("부모가 없는 카테고리가 정상적으로 수정된다.")
  void updateCategory_should_return_true_when_parentCategory_is_null() {
    when(categoryRepository.findCategoryById(updateWithoutParent.getId()))
      .thenReturn(Optional.of(testCategory2));
    when(categoryMapper.toResponse(any(Category.class)))
      .thenReturn(categoryResponse2);

    CategoryResponse result =
        categoryService.updateCategory(updateWithoutParent.getId(), updateWithoutParent);

    assertThat(result).isNotNull();
    assertThat(testCategory2.getName()).isEqualTo(updateWithoutParent.getName());

    verify(categoryRepository).findCategoryById(updateWithoutParent.getId());
    verify(categoryMapper).toResponse(any(Category.class));
  }

  @Test
  @DisplayName("자기자신을 부모 카테고리로 지정하면 수정 시 오류가 발생한다.")
  void updateCategory_should_throwException_when_parentCategory_is_self() {
    ReflectionTestUtils.setField(testCategory, "id", 30L);

    when(categoryRepository.findCategoryById(updateSelfParent.getId()))
      .thenReturn(Optional.of(testCategory));
    when(categoryRepository.findCategoryById(updateSelfParent.getParentId()))
      .thenReturn(Optional.of(testCategory));

    assertThatThrownBy(() -> categoryService.updateCategory(updateSelfParent.getId(), updateSelfParent))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_ALLOWED_SELF_PARENT.getMessage());

    verify(categoryRepository, times(2)).findCategoryById(anyLong());
  }

  @Test
  @DisplayName("하위 카테고리가 없으면 정상적으로 삭제된다.")
  void deleteCategory_should_return_true() {
    when(categoryRepository.findCategoryById(1L))
        .thenReturn(Optional.of(testCategory));
    when(categoryRepository.findCategoryByParentId(1L))
        .thenReturn(List.of());

    categoryService.deleteCategory(1L);

    assertThat(testCategory.getDeletedAt()).isNotNull();
    verify(categoryRepository).findCategoryById(1L);
    verify(categoryRepository).findCategoryByParentId(1L);
  }

  @Test
  @DisplayName("하위 카테고리가 있으면 오류가 발생한다.")
  void deleteCategory_should_throw_exception_when_parentCategory_is_null() {
    when(categoryRepository.findCategoryById(1L))
        .thenReturn(Optional.of(testCategory));
    when(categoryRepository.findCategoryByParentId(1L))
        .thenReturn(childCategoryList);

    assertThatThrownBy(() -> categoryService.deleteCategory(1L))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_DELETE_CATEGORY.getMessage());

    verify(categoryRepository).findCategoryById(1L);
    verify(categoryRepository).findCategoryByParentId(1L);
  }
}