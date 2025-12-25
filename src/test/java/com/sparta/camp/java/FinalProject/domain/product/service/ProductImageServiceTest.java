package com.sparta.camp.java.FinalProject.domain.product.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.camp.java.FinalProject.common.enums.SellStatus;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.category.entity.Category;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductImageResponse;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductImage;
import com.sparta.camp.java.FinalProject.domain.product.mapper.ProductImageMapper;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductImageRepository;
import com.sparta.camp.java.FinalProject.domain.product.vo.ColorOption;
import com.sparta.camp.java.FinalProject.domain.product.vo.ProductOption;
import com.sparta.camp.java.FinalProject.domain.product.vo.SizeOption;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

  @InjectMocks
  ProductImageService productImageService;

  @Mock
  private ProductImageRepository productImageRepository;

  @Mock
  private ProductImageMapper productImageMapper;

  private Product testProduct;
  private Category testCategory;

  private SizeOption smallSize;
  private SizeOption mediumSize;
  private ColorOption colorOption;
  private ColorOption colorOption2;
  private ProductOption productOption;

  private List<MultipartFile> productImageList = new ArrayList<>();

  @BeforeEach
  void setUp() throws IOException {
    String testDir = "build/test/upload/";
    Files.createDirectories(Paths.get(testDir));
    ReflectionTestUtils.setField(productImageService, "fileDir", testDir);

    testCategory = new Category();
    ReflectionTestUtils.setField(testCategory, "id", 1L);
    ReflectionTestUtils.setField(testCategory, "name", "test_category");

    smallSize = createSize("S", 10);
    mediumSize = createSize("M", 5);

    colorOption = createColor("white", List.of(smallSize, mediumSize));
    colorOption2 = createColor("black", List.of(mediumSize));

    productOption = createProductOption(List.of(colorOption, colorOption2));

    testProduct = createProduct(1L, testCategory, "test_product", 32000, "test", productOption);

    productImageList = List.of(
        mockImage("image1"),
        mockImage("image2"),
        mockImage("image3")
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

  private MockMultipartFile mockImage(String name) {
    return new MockMultipartFile(
        "images",
        name + ".jpg",
        "image/jpeg",
        "fake image content".getBytes()
    );
  }

  @Test
  @DisplayName("상품이미지가 정상적으로 등록 처리된다.")
  void createProductImageList_should_create_images_successfully() throws IOException {
    when(productImageRepository.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    productImageService.createProductImageList(testProduct, productImageList);

    verify(productImageRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("파일이름이 존재하지 않는 경우 오류가 발생한다.")
  void createProductImageList_should_throwException_when_imageName_does_not_exist() {
    List<MultipartFile> mockMultipartFileList = new ArrayList<>();
    mockMultipartFileList.add(new MockMultipartFile(
        "images",
        "",
        "image/jpeg",
        "fake image content".getBytes()
    ));

    assertThatThrownBy(() -> productImageService.createProductImageList(null, mockMultipartFileList))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_FILE.getMessage());

  }

  @Test
  @DisplayName("상품이미지가 정상적으로 수정 처리된다.")
  void updateProductImageList_should_return_updated_images() throws IOException {
    doNothing()
        .when(productImageRepository)
        .softDeleteByProductId(eq(1L), any(LocalDateTime.class));

    when(productImageRepository.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(productImageMapper.toResponse(any(ProductImage.class)))
        .thenReturn(ProductImageResponse.builder().build());

    List<ProductImageResponse> results =
        productImageService.updateProductImageList(testProduct, productImageList);

    assertThat(results).hasSize(productImageList.size());

    verify(productImageRepository).softDeleteByProductId(eq(1L), any(LocalDateTime.class));
    verify(productImageRepository).saveAll(anyList());
    verify(productImageMapper, times(productImageList.size()))
        .toResponse(any(ProductImage.class));

  }
}