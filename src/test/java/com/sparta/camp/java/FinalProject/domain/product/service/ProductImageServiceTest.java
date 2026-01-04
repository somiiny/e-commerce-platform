package com.sparta.camp.java.FinalProject.domain.product.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductImageResponse;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductImage;
import com.sparta.camp.java.FinalProject.domain.product.mapper.ProductImageMapper;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductImageRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

  private Product product;
  private List<MultipartFile> createFileList = new ArrayList<>();
  private List<MultipartFile> updateFileList = new ArrayList<>();

  @BeforeEach
  void setUp() throws IOException {
    String testDir = "build/test/upload/";
    Files.createDirectories(Paths.get(testDir));
    ReflectionTestUtils.setField(productImageService, "fileDir", testDir);

    product = Product.builder()
        .name("p1")
        .build();
    ReflectionTestUtils.setField(product, "id", 1L);

    createFileList = List.of(
        createMockImage("image1"),
        createMockImage("image2")
    );

    updateFileList = List.of(
        createMockImage("update1")
    );
  }


  private MockMultipartFile createMockImage(String name) {
    return new MockMultipartFile(
        "images",
        name + ".jpg",
        "image/jpeg",
        "fake image content".getBytes()
    );
  }

  @Test
  @DisplayName("상품이미지가 정상적으로 등록 처리된다.")
  void createProductImageList_should_create_images() throws IOException {

    when(productImageRepository.saveAll(anyList()))
        .thenAnswer(invocation -> {
            List<ProductImage> list = invocation.getArgument(0);

            long id = 1L;
            for (ProductImage image : list) {
              ReflectionTestUtils.setField(image, "id", id++);
            }

            return list;
        });

    productImageService.createProductImages(product, createFileList);

    ArgumentCaptor<List<ProductImage>> captor = ArgumentCaptor.forClass(List.class);
    verify(productImageRepository).saveAll(captor.capture());

    List<ProductImage> savedImages = captor.getValue();

    assertThat(savedImages).hasSize(createFileList.size());
    assertThat(savedImages.get(0).getProduct()).isEqualTo(product);
    assertThat(savedImages.get(0).getOriginalName())
        .isEqualTo(createFileList.get(0).getOriginalFilename());
    assertThat(savedImages.get(1).getProduct()).isEqualTo(product);
    assertThat(savedImages.get(1).getOriginalName())
        .isEqualTo(createFileList.get(1).getOriginalFilename());

  }

  @Test
  @DisplayName("파일이름이 존재하지 않는 경우 오류가 발생한다.")
  void createProductImageList_should_throwException_when_imageName_does_not_exist() {
    List<MultipartFile> emptyNameList = new ArrayList<>();
    emptyNameList.add(new MockMultipartFile(
        "images",
        "",
        "image/jpeg",
        "fake image content".getBytes()
    ));

    assertThatThrownBy(() -> productImageService.createProductImages(product, emptyNameList))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_FILE.getMessage());

    verify(productImageRepository, never()).saveAll(anyList());
  }

  @Test
  @DisplayName("상품이미지가 정상적으로 수정 처리된다.")
  void updateProductImageList_should_return_updated_images() throws IOException {
    doNothing()
        .when(productImageRepository)
        .softDeleteByProductId(product.getId());

    when(productImageRepository.saveAll(anyList()))
        .thenAnswer(invocation -> {
          List<ProductImage> list = invocation.getArgument(0);

          long id = 1L;
          for (ProductImage image : list) {
            ReflectionTestUtils.setField(image, "id", id++);
          }

          return list;
        });

    when(productImageMapper.toResponse(any(ProductImage.class)))
        .thenAnswer(invocation -> {
          ProductImage image = invocation.getArgument(0);
          return ProductImageResponse.builder()
              .id(image.getId())
              .originalName(image.getOriginalName())
              .storedName(image.getStoredName())
              .build();
        });

    List<ProductImageResponse> results =
        productImageService.updateProductImages(product, updateFileList);

    assertThat(results).hasSize(updateFileList.size());

    verify(productImageRepository).softDeleteByProductId(product.getId());
    verify(productImageRepository).saveAll(anyList());
    verify(productImageMapper, times(updateFileList.size()))
        .toResponse(any(ProductImage.class));

    assertThat(results.get(0).getOriginalName())
        .isEqualTo(updateFileList.get(0).getOriginalFilename());

  }
}