package com.sparta.camp.java.FinalProject.domain.product.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.camp.java.FinalProject.common.enums.ColorType;
import com.sparta.camp.java.FinalProject.common.enums.SizeType;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.category.entity.Category;
import com.sparta.camp.java.FinalProject.domain.category.repository.CategoryRepository;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductCreateRequest;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductOptionRequest;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductImageRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductOptionRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseProductQueryRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
class ProductAdminServiceTest {

  @InjectMocks
  private ProductAdminService productAdminService;

  @Mock
  private ProductImageService productImageService;

  @Mock
  private ProductOptionService productOptionService;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductImageRepository productImageRepository;

  @Mock
  private PurchaseProductQueryRepository purchaseProductQueryRepository;

  @Mock
  private ProductOptionRepository productOptionRepository;

  private ProductCreateRequest productCreateRequest;
  private ProductUpdateRequest productUpdateRequest;
  private Category category;
  private Product product;

  @BeforeEach
  void setUp() throws IOException {
    category = new Category();
    ReflectionTestUtils.setField(category, "id", 1L);
    ReflectionTestUtils.setField(category, "name", "c1");

    product = new Product();
    ReflectionTestUtils.setField(product, "id", 1L);
    ReflectionTestUtils.setField(product, "name", "p1");

    productCreateRequest = new ProductCreateRequest();
    ReflectionTestUtils.setField(productCreateRequest, "categoryId", category.getId());
    ReflectionTestUtils.setField(productCreateRequest, "name", "create");
    ReflectionTestUtils.setField(productCreateRequest, "options", List.of(
        createOptionRequest(ColorType.WHITE, SizeType.LARGE, 5),
        createOptionRequest(ColorType.BLACK, SizeType.MEDIUM, 10)
    ));
    ReflectionTestUtils.setField(productCreateRequest, "images", List.of(
        createMultipartFile("image1"),
        createMultipartFile("image2")
    ));

    productUpdateRequest = new ProductUpdateRequest();
    ReflectionTestUtils.setField(productUpdateRequest, "id", 1L);
    ReflectionTestUtils.setField(productUpdateRequest, "categoryId", category.getId());
    ReflectionTestUtils.setField(productUpdateRequest, "name", "update");
    ReflectionTestUtils.setField(productUpdateRequest, "options", List.of(
        createOptionRequest(ColorType.WHITE, SizeType.LARGE, 1),
        createOptionRequest(ColorType.BLACK, SizeType.MEDIUM, 9)
    ));
    ReflectionTestUtils.setField(productUpdateRequest, "images", List.of(
        createMultipartFile("image1"),
        createMultipartFile("image2")
    ));

  }

  private ProductOptionRequest createOptionRequest(ColorType color, SizeType sizeType, Integer stock) {
    ProductOptionRequest request = new ProductOptionRequest();
    ReflectionTestUtils.setField(request, "color", color);
    ReflectionTestUtils.setField(request, "size", sizeType);
    ReflectionTestUtils.setField(request, "stock", stock);
    return request;
  }

  private MockMultipartFile createMultipartFile(String name) throws IOException {
    MockMultipartFile multipartFile = new MockMultipartFile(
        "images",
        name+".jpg",
        "image/jpeg",
        "fake image content".getBytes());
    return multipartFile;
  }

  @Test
  @DisplayName("정상적으로 상품 생성이 된다.")
  void createProduct_should_return_createdProductId() throws IOException {

    when(categoryRepository.findCategoryById(productCreateRequest.getCategoryId()))
        .thenReturn(Optional.of(category));
    when(productRepository.findProductByName(productCreateRequest.getName()))
        .thenReturn(Optional.empty());
    when(productRepository.save(any(Product.class)))
        .thenAnswer(invocation -> {
          Product p = invocation.getArgument(0);
          ReflectionTestUtils.setField(p, "id", 100L);
          return p;
        });

    Long result = productAdminService.createProduct(productCreateRequest);

    assertThat(result).isEqualTo(100L);

    verify(categoryRepository).findCategoryById(productCreateRequest.getCategoryId());
    verify(productRepository).findProductByName(productCreateRequest.getName());
    verify(productRepository).save(any(Product.class));
    verify(productOptionService).createProductOption(any(Product.class), anyList());
    verify(productImageService).createProductImages(any(Product.class), anyList());

  }

  @Test
  @DisplayName("카테고리가 존재하지 않는 경우 오류가 발생 한다.")
  void createProduct_should_throwException_when_category_is_not_existed() {
    when(categoryRepository.findCategoryById(productUpdateRequest.getCategoryId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> productAdminService.createProduct(productCreateRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_CATEGORY.getMessage());

    verify(categoryRepository).findCategoryById(productCreateRequest.getCategoryId());
  }

  @Test
  @DisplayName("동일한 상품명 존재 시 오류가 발생 한다.")
  void createProduct_should_throwException_when_productName_already_exists() throws IOException {
    when(categoryRepository.findCategoryById(productCreateRequest.getCategoryId()))
        .thenReturn(Optional.of(category));
    when(productRepository.findProductByName(productCreateRequest.getName()))
        .thenReturn(Optional.of(product));

    assertThatThrownBy(() -> productAdminService.createProduct(productCreateRequest))
    .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.DUPLICATE_PRODUCT_NAME.getMessage());

    verify(categoryRepository).findCategoryById(productCreateRequest.getCategoryId());
    verify(productRepository).findProductByName(productCreateRequest.getName());
    verify(productOptionService, never()).createProductOption(any(), any());
    verify(productImageService, never()).createProductImages(any(), any());
  }

  @Test
  @DisplayName("정상적으로 상품 수정이 된다.")
  void updateProduct_should_return_updatedProduct() throws IOException {

    when(productRepository.findProductById(product.getId())).thenReturn(Optional.of(product));
    when(categoryRepository.findCategoryById(productUpdateRequest.getCategoryId())).thenReturn(Optional.of(category));
    when(productRepository.findProductByIdAndName(product.getId(), productUpdateRequest.getName())).thenReturn(Optional.empty());

    ProductResponse result = productAdminService.updateProduct(product.getId(), productUpdateRequest);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(product.getId());
    assertThat(result.getName()).isEqualTo(productUpdateRequest.getName());

    verify(productRepository).findProductById(product.getId());
    verify(categoryRepository).findCategoryById(productUpdateRequest.getCategoryId());
    verify(productRepository).findProductByIdAndName(product.getId(), productUpdateRequest.getName());
    verify(productOptionService).updateProductOption(any(), anyList());
    verify(productImageService).updateProductImages(any(), anyList());
  }

  @Test
  @DisplayName("해당 상품이 존재하지 않는 경우 오류가 발생한다.")
  void updateProduct_should_throwException_when_product_is_not_existed() throws IOException {
    when(productRepository.findProductById(productUpdateRequest.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> productAdminService.updateProduct(productUpdateRequest.getId(), productUpdateRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PRODUCT.getMessage());

    verify(productRepository).findProductById(productUpdateRequest.getId());
    verify(productOptionService, never()).updateProductOption(any(), any());
    verify(productImageService, never()).updateProductImages(any(), any());
  }

  @Test
  @DisplayName("해당 상품 제외 동일 상품명 존재시 오류가 발생한다.")
  void updateProduct_should_throwException_when_productName_already_exists() throws IOException {
    when(productRepository.findProductById(productUpdateRequest.getId()))
        .thenReturn(Optional.of(product));
    when(categoryRepository.findCategoryById(productUpdateRequest.getCategoryId()))
        .thenReturn(Optional.of(category));
    when(productRepository.findProductByIdAndName(product.getId(), productUpdateRequest.getName()))
        .thenReturn(Optional.of(product));

    assertThatThrownBy(() -> productAdminService.updateProduct(product.getId(), productUpdateRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.DUPLICATE_PRODUCT_NAME.getMessage());

    verify(productRepository).findProductById(productUpdateRequest.getId());
    verify(categoryRepository).findCategoryById(productUpdateRequest.getCategoryId());
    verify(productRepository).findProductByIdAndName(product.getId(), productUpdateRequest.getName());
    verify(productOptionService, never()).updateProductOption(any(), any());
    verify(productImageService, never()).updateProductImages(any(), any());
  }

  @Test
  @DisplayName("해당 상품이 정상적으로 삭제 처리 된다.")
  void deleteProduct_should_delete_product_successfully() {

    when(purchaseProductQueryRepository.existsUndeletableProducts(product.getId()))
        .thenReturn(false);
    when(productRepository.findProductById(product.getId())).thenReturn(Optional.of(product));
    doNothing()
        .when(productOptionRepository)
        .softDeleteByProductId(product.getId());
    doNothing()
        .when(productImageRepository)
        .softDeleteByProductId(product.getId());

    productAdminService.deleteProduct(product.getId());

    assertThat(product.getDeletedAt()).isNotNull();

    verify(purchaseProductQueryRepository).existsUndeletableProducts(product.getId());
    verify(productRepository).findProductById(product.getId());
    verify(productOptionRepository).softDeleteByProductId(product.getId());
    verify(productImageRepository).softDeleteByProductId(product.getId());
  }

  @Test
  @DisplayName("해당 상품이 삭제가 불가능한 상태이면 오류가 발생 한다.")
  void deleteProduct_should_throwException_when_product_is_undeletable() {
    when(purchaseProductQueryRepository.existsUndeletableProducts(product.getId()))
        .thenReturn(true);

    assertThatThrownBy(() -> productAdminService.deleteProduct(product.getId()))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.CANNOT_DELETE_PRODUCT.getMessage());

    assertThat(product.getDeletedAt()).isNull();

    verify(purchaseProductQueryRepository).existsUndeletableProducts(product.getId());
    verify(productRepository, never()).findProductById(product.getId());
    verify(productOptionRepository, never()).softDeleteByProductId(product.getId());
    verify(productImageRepository, never()).softDeleteByProductId(product.getId());

  }
}