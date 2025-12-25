package com.sparta.camp.java.FinalProject.domain.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sparta.camp.java.FinalProject.common.enums.SellStatus;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationResponse;
import com.sparta.camp.java.FinalProject.domain.category.entity.Category;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductImageResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductSearchRequest;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductImage;
import com.sparta.camp.java.FinalProject.domain.product.mapper.ProductImageMapper;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductImageRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductQueryRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
import com.sparta.camp.java.FinalProject.domain.product.vo.ColorOption;
import com.sparta.camp.java.FinalProject.domain.product.vo.ProductOption;
import com.sparta.camp.java.FinalProject.domain.product.vo.SizeOption;
import java.math.BigDecimal;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @InjectMocks
  private ProductService productService;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductQueryRepository productQueryRepository;

  @Mock
  private ProductImageRepository productImageRepository;

  @Mock
  private ProductImageMapper productImageMapper;

  private ProductSearchRequest productSearchRequest;
  private PaginationRequest paginationRequest;

  private Category category;
  private Product testProduct;

  private SizeOption smallSize;
  private ColorOption colorOption;
  private ProductOption productOption;

  private List<Product> products = new ArrayList<>();
  private List<ProductImage> productImageList = new ArrayList<>();

  @BeforeEach
  void setUp() {
    productSearchRequest = new ProductSearchRequest();

    paginationRequest = new PaginationRequest();
    ReflectionTestUtils.setField(paginationRequest, "page", 1);
    ReflectionTestUtils.setField(paginationRequest, "size", 10);

    category = new Category();
    ReflectionTestUtils.setField(category, "id", 1L);
    ReflectionTestUtils.setField(category, "name", "test_category");

    smallSize = createSize("S", 10);
    colorOption = createColor("white", List.of(smallSize));
    productOption = createProductOption(List.of(colorOption));

    testProduct = createProduct(1L, category, "test_product", 32000, "test", productOption);

    products = List.of(
      createProduct(2L, category, "test2", 20000, "test2", productOption),
      createProduct(3L, category, "test3", 30000, "test3", productOption)
    );

    productImageList = List.of(
        ProductImage.builder()
            .product(testProduct)
            .originalName("test")
            .storedName("test")
            .uploadPath("/test")
            .sortOrder(1)
            .isMain(true)
            .build()
    );
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
  @DisplayName("정상적으로 모든 상품 조회를 한다.")
  void getAllProducts_should_return_all_products() {
    products.get(0).getProductImageList().add(productImageList.get(0));
    products.get(1).getProductImageList().add(productImageList.get(0));

    when(productQueryRepository.findProducts(any(ProductSearchRequest.class), any(PaginationRequest.class)))
        .thenReturn(products);
    when(productImageMapper.toResponse(any(ProductImage.class)))
        .thenReturn(ProductImageResponse.builder().build());
    when(productQueryRepository.countProducts(any(ProductSearchRequest.class)))
        .thenReturn((long) products.size());

    PaginationResponse<ProductResponse> results = productService.getAllProducts(productSearchRequest, paginationRequest);

    assertThat(results).isNotNull();
    assertThat(results.getContent().size()).isEqualTo(products.size());
    assertThat(results.getTotalItems()).isEqualTo(products.size());
    assertThat(results.getContent().get(0).getProductImageResponseList().size()).isEqualTo(1);

    verify(productQueryRepository).findProducts(any(ProductSearchRequest.class), any(PaginationRequest.class));
    verify(productQueryRepository).countProducts(any(ProductSearchRequest.class));
    verify(productImageMapper, times(2)).toResponse(any(ProductImage.class));
  }

  @Test
  @DisplayName("상품이 존재하지 않으면 오류가 발생한다.")
  void getAllProducts_should_throwException_when_nonExistentProducts() {
    when(productQueryRepository.findProducts(any(ProductSearchRequest.class), any(PaginationRequest.class)))
        .thenReturn(List.of());

    assertThatThrownBy(() -> productService.getAllProducts(productSearchRequest, paginationRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PRODUCT.getMessage());

    verify(productQueryRepository).findProducts(any(ProductSearchRequest.class), any(PaginationRequest.class));
    verifyNoMoreInteractions(productQueryRepository);
  }

  @Test
  @DisplayName("정상적으로 상품 조회를 한다.")
  void getProductById_should_return_product() {
    when(productRepository.findProductById(anyLong())).thenReturn(Optional.of(testProduct));
    when(productImageRepository.findAllByProductId(anyLong()))
        .thenReturn(productImageList);
    when(productImageMapper.toResponse(any(ProductImage.class)))
        .thenReturn(ProductImageResponse.builder().build());

    ProductResponse result = productService.getProductById(testProduct.getId());

    assertThat(result).isNotNull();

    verify(productRepository).findProductById(anyLong());
    verify(productImageRepository).findAllByProductId(anyLong());
    verify(productImageMapper, times(productImageList.size())).toResponse(any(ProductImage.class));
  }

  @Test
  @DisplayName("상품이 존재하지 않으면 오류가 발생한다.")
  void getProductById_should_throwException_when_nonExistentProduct() {
    when(productRepository.findProductById(anyLong())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> productService.getProductById(testProduct.getId()))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PRODUCT.getMessage());
    verify(productRepository).findProductById(anyLong());
  }

  @Test
  @DisplayName("상품이미지가 존재하지 않으면 오류가 발생한다.")
  void getProductById_should_throwException_when_nonExistentProductImage() {
    when(productRepository.findProductById(anyLong())).thenReturn(Optional.of(testProduct));
    when(productImageRepository.findAllByProductId(anyLong()))
        .thenReturn(List.of());

    assertThatThrownBy(() -> productService.getProductById(testProduct.getId()))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PRODUCT_IMAGE.getMessage());

    verify(productRepository).findProductById(anyLong());
    verify(productImageRepository).findAllByProductId(anyLong());
  }
}