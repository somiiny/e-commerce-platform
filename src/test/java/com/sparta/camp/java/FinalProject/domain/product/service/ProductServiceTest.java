package com.sparta.camp.java.FinalProject.domain.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductSearchRequest;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductImage;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductOption;
import com.sparta.camp.java.FinalProject.domain.product.mapper.ProductMapper;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductImageRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductOptionRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductQueryRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
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
  private ProductOptionRepository productOptionRepository;

  @Mock
  private ProductImageRepository productImageRepository;

  @Mock
  private ProductMapper productMapper;

  private PaginationRequest paginationRequest;

  private Product product1;
  private Product product2;
  private ProductResponse productResponse1;
  private ProductResponse productResponse2;

  private List<Product> products = new ArrayList<>();
  private List<ProductOption> productOptions = new ArrayList<>();
  private List<ProductImage> productImages = new ArrayList<>();

  @BeforeEach
  void setUp() {
    paginationRequest = new PaginationRequest();
    ReflectionTestUtils.setField(paginationRequest, "page", 1);
    ReflectionTestUtils.setField(paginationRequest, "size", 10);

    product1 = createProduct(1L, "p1");
    product2 = createProduct(2L, "p2");
    products = List.of(product1, product2);

    ProductOption productOption1 = createProductOption(1L, product1);
    ProductOption productOption2 = createProductOption(2L, product2);
    productOptions = List.of(productOption1, productOption2);

    ProductImage productImage1 = createProductImage(1L, product1);
    ProductImage productImage2 = createProductImage(2L, product2);
    productImages = List.of(productImage1, productImage2);

    productResponse1 = ProductResponse.builder().id(1L).name("p1").build();
    productResponse2 = ProductResponse.builder().id(2L).name("p2").build();
  }

  private Product createProduct(Long id, String name) {
    Product product = new Product();
    ReflectionTestUtils.setField(product, "id", id);
    ReflectionTestUtils.setField(product, "name", name);
    return product;
  }

  private ProductOption createProductOption(Long id, Product product) {
    ProductOption productOption = new ProductOption();
    ReflectionTestUtils.setField(productOption, "id", id);
    ReflectionTestUtils.setField(productOption, "product", product);
    return productOption;
  }

  private ProductImage createProductImage(Long id, Product product) {
    ProductImage productImage = new ProductImage();
    ReflectionTestUtils.setField(productImage, "id", id);
    ReflectionTestUtils.setField(productImage, "product", product);
    return productImage;
  }

  @Test
  @DisplayName("정상적으로 모든 상품 조회를 한다.")
  void getAllProducts_should_return_all_products() {

    when(productQueryRepository.findProducts(any(), any())).thenReturn(products);
    when(productOptionRepository.findProductOptionInProductIds(anyList())).thenReturn(productOptions);
    when(productImageRepository.findProductImageInProductIds(anyList())).thenReturn(productImages);

    when(productMapper.toResponse(product1)).thenReturn(productResponse1);
    when(productMapper.toResponse(product2)).thenReturn(productResponse2);

    when(productQueryRepository.countProducts(any())).thenReturn(2L);

    PaginationResponse<ProductResponse> results =
        productService.getAllProducts(new ProductSearchRequest(), paginationRequest);

    assertThat(results).isNotNull();
    assertThat(results.getTotalItems()).isEqualTo(2);
    assertThat(results.getContent().size()).isEqualTo(2);

    ProductResponse response1 = results.getContent().get(0);
    assertThat(response1.getId()).isEqualTo(productResponse1.getId());
    assertThat(response1.getProductOptions().size()).isEqualTo(1);
    assertThat(response1.getProductImages().size()).isEqualTo(1);

    ProductResponse response2 = results.getContent().get(1);
    assertThat(response2.getId()).isEqualTo(productResponse2.getId());
    assertThat(response2.getProductOptions().size()).isEqualTo(1);
    assertThat(response2.getProductImages().size()).isEqualTo(1);

    verify(productQueryRepository).findProducts(any(), any());
    verify(productOptionRepository).findProductOptionInProductIds(anyList());
    verify(productImageRepository).findProductImageInProductIds(anyList());
    verify(productMapper, times(2)).toResponse(any(Product.class));
    verify(productQueryRepository).countProducts(any());
  }

  @Test
  @DisplayName("상품이 존재하지 않으면 빈 리스트를 리턴한다.")
  void getAllProducts_should_return_empty_list_when_nonExistentProducts() {

    when(productQueryRepository.findProducts(any(), any())).thenReturn(List.of());
    when(productQueryRepository.countProducts(any(ProductSearchRequest.class))).thenReturn(0L);

    PaginationResponse result = productService.getAllProducts(new ProductSearchRequest(), paginationRequest);

    assertThat(result).isNotNull();
    assertThat(result.getTotalItems()).isEqualTo(0);
    assertThat(result.getContent().size()).isEqualTo(0);

    verify(productQueryRepository).findProducts(any(), any());
    verify(productQueryRepository).countProducts(any(ProductSearchRequest.class));
    verifyNoMoreInteractions(productOptionRepository);
    verifyNoMoreInteractions(productImageRepository);
  }

  @Test
  @DisplayName("정상적으로 상품 조회를 한다.")
  void getProductById_should_return_product() {
    ReflectionTestUtils.setField(product1, "productOptions", productOptions);
    ReflectionTestUtils.setField(product1, "productImages", productImages);

    when(productRepository.findProductById(anyLong())).thenReturn(Optional.of(product1));
    when(productMapper.toResponse(product1)).thenReturn(productResponse1);

    ProductResponse result = productService.getProductById(product1.getId());

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(productResponse1.getId());
    assertThat(result.getProductOptions().size()).isEqualTo(2);
    assertThat(result.getProductImages().size()).isEqualTo(2);

    verify(productRepository).findProductById(product1.getId());
    verify(productMapper).toResponse(product1);
  }

  @Test
  @DisplayName("상품이 존재하지 않으면 오류가 발생한다.")
  void getProductById_should_throwException_when_nonExistentProduct() {
    when(productRepository.findProductById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.getProductById(0L))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PRODUCT.getMessage());

    verify(productRepository).findProductById(anyLong());
  }

  @Test
  @DisplayName("상품 옵션이 존재하지 않으면 오류가 발생한다.")
  void getProductById_should_throwException_when_nonExistentProductOption() {

    when(productRepository.findProductById(anyLong())).thenReturn(Optional.of(product1));

    assertThatThrownBy(() -> productService.getProductById(product1.getId()))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PRODUCT_OPTIONS.getMessage());

    verify(productRepository).findProductById(product1.getId());
  }

  @Test
  @DisplayName("상품이미지가 존재하지 않으면 오류가 발생한다.")
  void getProductById_should_throwException_when_nonExistentProductImage() {
    ReflectionTestUtils.setField(product1, "productOptions", productOptions);

    when(productRepository.findProductById(anyLong())).thenReturn(Optional.of(product1));

    assertThatThrownBy(() -> productService.getProductById(product1.getId()))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PRODUCT_IMAGE.getMessage());

    verify(productRepository).findProductById(product1.getId());
  }

}