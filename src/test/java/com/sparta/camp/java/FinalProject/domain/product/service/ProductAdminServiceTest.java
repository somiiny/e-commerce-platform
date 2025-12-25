package com.sparta.camp.java.FinalProject.domain.product.service;

import com.sparta.camp.java.FinalProject.common.enums.SellStatus;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.category.entity.Category;
import com.sparta.camp.java.FinalProject.domain.category.repository.CategoryRepository;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductCreateRequest;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductImageResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductImage;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductImageRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
import com.sparta.camp.java.FinalProject.domain.product.vo.ColorOption;
import com.sparta.camp.java.FinalProject.domain.product.vo.ProductOption;
import com.sparta.camp.java.FinalProject.domain.product.vo.SizeOption;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseProductQueryRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductAdminServiceTest {

  @InjectMocks
  private ProductAdminService productAdminService;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductImageService productImageService;

  @Mock
  private ProductImageRepository productImageRepository;

  @Mock
  private PurchaseProductQueryRepository purchaseProductQueryRepository;

  private ProductCreateRequest productCreateRequest;
  private ProductUpdateRequest productUpdateRequest;

  private ProductOption productOption;
  private ProductOption productOption2;

  private ColorOption colorOption;
  private ColorOption colorOption2;
  private ColorOption colorOption3;

  private SizeOption smallSize;
  private SizeOption mediumSize;
  private SizeOption largeSize;

  private Category category;

  private Product testProduct;
  private ProductImage newProductImage;
  private List<ProductImageResponse> updateImageList;

  @BeforeEach
  void setUp() {
    smallSize = createSize("S", 10);
    mediumSize = createSize("M", 5);
    largeSize = createSize("L", 15);

    colorOption = createColor("white", List.of(smallSize, mediumSize, largeSize));
    colorOption2 = createColor("black", List.of(mediumSize, largeSize));
    colorOption3 = createColor("navy", List.of(smallSize, mediumSize));

    productOption = createProductOption(List.of(colorOption, colorOption2));
    productOption2 = createProductOption(List.of(colorOption2, colorOption3));

    MockMultipartFile image = new MockMultipartFile(
        "images",
        "test-image.jpg",
        "image/jpeg",
        "fake image content".getBytes()
    );

    productCreateRequest = createProductCreateRequest(1L,
        "create_test",
        32000,
        5,
        "clothing",
        productOption,
        List.of(image));

    productUpdateRequest = createProductUpdateRequest(1L,
        1L,
        "update_test",
        28000,
        3,
        "clothing2",
        productOption2,
        List.of(image));


    category = new Category();
    ReflectionTestUtils.setField(category, "id", 1L);
    ReflectionTestUtils.setField(category, "name", "test_category");

    ProductImageResponse imageResponse = ProductImageResponse.builder()
        .id(1L)
        .productId(1L)
        .originalName(image.getName())
        .storedName("test")
        .uploadPath("test")
        .sortOrder(1)
        .isMain(true)
        .build();
    updateImageList = new ArrayList<>();
    updateImageList.add(imageResponse);

    testProduct = createProduct(1L, category, "test_product", 32000, "test", productOption);

  }

  private SizeOption createSize(String name, int stock) {
    SizeOption size = new SizeOption();
    ReflectionTestUtils.setField(size, "sizeName", name);
    ReflectionTestUtils.setField(size, "stock", stock);
    return size;
  }

  private ColorOption createColor(String name, List<SizeOption> sizes) {
    ColorOption colorOption = new ColorOption();
    ReflectionTestUtils.setField(colorOption, "colorName", name);
    ReflectionTestUtils.setField(colorOption, "sizes", new ArrayList<>(sizes));
    return colorOption;
  }

  private ProductOption createProductOption(List<ColorOption> colorOptions) {
    ProductOption productOption = new ProductOption();
    ReflectionTestUtils.setField(productOption, "colors", new ArrayList<>(colorOptions));
    return productOption;
  }

  private ProductCreateRequest createProductCreateRequest(
      Long categoryId,
      String name,
      Integer price,
      Integer stock,
      String description,
      ProductOption option,
      List<MultipartFile> images
  ) {
    ProductCreateRequest request = new ProductCreateRequest();
    ReflectionTestUtils.setField(request, "categoryId", categoryId);
    ReflectionTestUtils.setField(request, "name", name);
    ReflectionTestUtils.setField(request, "price", new BigDecimal(price));
    ReflectionTestUtils.setField(request, "stock", stock);
    ReflectionTestUtils.setField(request, "description", description);
    ReflectionTestUtils.setField(request, "options", option);
    ReflectionTestUtils.setField(request, "sellStatus", SellStatus.ON_SALE);
    ReflectionTestUtils.setField(request, "imageList", images);
    return request;
  }

  private ProductUpdateRequest createProductUpdateRequest(
      Long productId,
      Long categoryId,
      String name,
      Integer price,
      Integer stock,
      String description,
      ProductOption option,
      List<MultipartFile> images
  ) {
    ProductUpdateRequest request = new ProductUpdateRequest();
    ReflectionTestUtils.setField(request, "id", productId);
    ReflectionTestUtils.setField(request, "categoryId", categoryId);
    ReflectionTestUtils.setField(request, "name", name);
    ReflectionTestUtils.setField(request, "price", new BigDecimal(price));
    ReflectionTestUtils.setField(request, "stock", stock);
    ReflectionTestUtils.setField(request, "description", description);
    ReflectionTestUtils.setField(request, "options", option);
    ReflectionTestUtils.setField(request, "sellStatus", SellStatus.ON_SALE);
    ReflectionTestUtils.setField(request, "imageList", List.of(images));
    return request;
  }

  private Product createProduct(
      Long productId,
      Category category,
      String name,
      Integer price,
      String description,
      ProductOption option
  ) {
    Product product = new Product();
    ReflectionTestUtils.setField(product, "id", productId);
    ReflectionTestUtils.setField(product, "category", category);
    ReflectionTestUtils.setField(product, "name", name);
    ReflectionTestUtils.setField(product, "price", new BigDecimal(price));
    ReflectionTestUtils.setField(product, "description", description);
    ReflectionTestUtils.setField(product, "options", option);
    ReflectionTestUtils.setField(product, "sellStatus", SellStatus.ON_SALE);
    return product;
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
          Product arg = invocation.getArgument(0);
          ReflectionTestUtils.setField(arg, "id", 999L);
          return arg;
        });
    doNothing()
        .when(productImageService)
        .createProductImageList(any(Product.class), anyList());

    Long result = productAdminService.createProduct(productCreateRequest);

    assertThat(result).isNotNull();

    verify(categoryRepository).findCategoryById(productCreateRequest.getCategoryId());
    verify(productRepository).findProductByName(productCreateRequest.getName());
    verify(productRepository).save(any(Product.class));
    verify(productImageService).createProductImageList(any(Product.class), anyList());

  }

  @Test
  @DisplayName("똑같은 상품명 존재시 오류가 발생 한다.")
  void createProduct_should_throwException_when_productName_already_exists() {
    when(categoryRepository.findCategoryById(productCreateRequest.getCategoryId()))
        .thenReturn(Optional.of(category));
    when(productRepository.findProductByName(productCreateRequest.getName()))
        .thenReturn(Optional.of(testProduct));

    assertThatThrownBy(() -> productAdminService.createProduct(productCreateRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.DUPLICATE_PRODUCT_NAME.getMessage());

    verifyNoMoreInteractions(
        categoryRepository,
        productRepository
    );
  }

  @Test
  @DisplayName("정상적으로 상품 수정이 된다.")
  void updateProduct_should_return_updatedProduct() throws IOException {
    when(productRepository.findProductById(productUpdateRequest.getId()))
      .thenReturn(Optional.of(testProduct));
    when(categoryRepository.findCategoryById(productUpdateRequest.getCategoryId()))
      .thenReturn(Optional.of(category));
    when(productRepository.findProductByName(productUpdateRequest.getId(), productUpdateRequest.getName()))
        .thenReturn(Optional.empty());
    when(productImageService.updateProductImageList(any(Product.class), anyList()))
        .thenReturn(updateImageList);

    ProductResponse result = productAdminService.updateProduct(productUpdateRequest.getId(), productUpdateRequest);

    assertThat(result).isNotNull();
    verify(productRepository).findProductById(productUpdateRequest.getId());
    verify(categoryRepository).findCategoryById(productUpdateRequest.getCategoryId());
    verify(productRepository).findProductByName(productUpdateRequest.getId(), productUpdateRequest.getName());
    verify(productImageService).updateProductImageList(any(Product.class), anyList());
  }

  @Test
  @DisplayName("해당 상품이 존재하지 않는 경우 오류가 발생한다.")
  void updateProduct_should_throwException_when_product_is_not_existed() {
    when(productRepository.findProductById(productUpdateRequest.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> productAdminService.updateProduct(productUpdateRequest.getId(), productUpdateRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PRODUCT.getMessage());

    verifyNoMoreInteractions(productRepository);
  }

  @Test
  @DisplayName("해당 상품 제외 동일 상품명 존재시 오류가 발생한다.")
  void updateProduct_should_throwException_when_productName_already_exists() {
    when(productRepository.findProductById(productUpdateRequest.getId()))
        .thenReturn(Optional.of(testProduct));
    when(categoryRepository.findCategoryById(productUpdateRequest.getCategoryId()))
        .thenReturn(Optional.of(category));
    when(productRepository.findProductByName(productUpdateRequest.getId(), productUpdateRequest.getName()))
        .thenReturn(Optional.of(testProduct));

    assertThatThrownBy(() -> productAdminService.updateProduct(productUpdateRequest.getId(), productUpdateRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.DUPLICATE_PRODUCT_NAME.getMessage());

    verify(productRepository).findProductById(productUpdateRequest.getId());
    verify(categoryRepository).findCategoryById(productUpdateRequest.getCategoryId());
    verify(productRepository).findProductByName(productUpdateRequest.getId(), productUpdateRequest.getName());
    verifyNoMoreInteractions(
        productRepository,
        categoryRepository
    );
  }

  @Test
  @DisplayName("해당 상품이 정상적으로 삭제 처리 된다.")
  void deleteProduct_should_delete_product_successfully(){
    when(purchaseProductQueryRepository.existsUndeletableProducts(1L))
        .thenReturn(false);
    when(productRepository.findProductById(1L)).thenReturn(Optional.of(testProduct));
    doNothing()
        .when(productImageRepository)
        .softDeleteByProductId(eq(1L), any(LocalDateTime.class));

    productAdminService.deleteProduct(1L);

    assertThat(testProduct.getDeletedAt()).isNotNull();
    verify(purchaseProductQueryRepository).existsUndeletableProducts(1L);
    verify(productRepository).findProductById(1L);
    verify(productImageRepository).softDeleteByProductId(eq(1L), any(LocalDateTime.class));

  }

  @Test
  @DisplayName("해당 상품이 삭제가 불가능한 상태이면 오류가 발생 한다.")
  void deleteProduct_should_throwException_when_product_is_undeletable() {
    when(purchaseProductQueryRepository.existsUndeletableProducts(1L))
        .thenReturn(true);

    assertThatThrownBy(() -> productAdminService.deleteProduct(1L))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.CANNOT_DELETE_PRODUCT.getMessage());

    verify(purchaseProductQueryRepository).existsUndeletableProducts(1L);
    verifyNoMoreInteractions(
        purchaseProductQueryRepository
    );
  }
}