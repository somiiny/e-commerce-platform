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

import com.sparta.camp.java.FinalProject.common.enums.ColorType;
import com.sparta.camp.java.FinalProject.common.enums.SizeType;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductOptionRequest;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductOptionResponse;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductOption;
import com.sparta.camp.java.FinalProject.domain.product.mapper.ProductOptionMapper;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductOptionRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductOptionServiceTest {

  @InjectMocks
  private ProductOptionService productOptionService;

  @Mock
  private ProductOptionRepository productOptionRepository;

  @Mock
  private ProductOptionMapper productOptionMapper;

  private Product product;
  private List<ProductOptionRequest> productOptionRequests = new ArrayList<>();
  private List<ProductOptionRequest> optionUpdateRequests = new ArrayList<>();
  private List<ProductOptionResponse> optionResponses = new ArrayList<>();

  @BeforeEach
  void setUp() {

    product = Product.builder()
        .name("p1")
        .build();
    ReflectionTestUtils.setField(product, "id", 1L);

    productOptionRequests.add(createOptionRequest(ColorType.WHITE, SizeType.LARGE, 5));
    productOptionRequests.add(createOptionRequest(ColorType.BLACK, SizeType.SMALL, 3));

    optionUpdateRequests = List.of(
      createOptionRequest(ColorType.WHITE, SizeType.MEDIUM, 3),
      createOptionRequest(ColorType.BLACK, SizeType.LARGE, 5)
    );

  }

  private ProductOptionRequest createOptionRequest(ColorType color,
      SizeType size, Integer stock) {
    ProductOptionRequest productOption = new ProductOptionRequest();
    ReflectionTestUtils.setField(productOption, "color", color);
    ReflectionTestUtils.setField(productOption, "size", size);
    ReflectionTestUtils.setField(productOption, "stock", stock);
    return productOption;
  }

  @Test
  @DisplayName("상품 옵션이 정상 등록된다.")
  void createProductOption_should_create_productOption() {
    when(productOptionRepository.saveAll(anyList()))
        .thenAnswer(invocation -> {
          List<ProductOption> list = invocation.getArgument(0);
          return list;
        });

    productOptionService.createProductOption(product, productOptionRequests);

    ArgumentCaptor<List<ProductOption>> captor = ArgumentCaptor.forClass(List.class);
    verify(productOptionRepository).saveAll(captor.capture());

    List<ProductOption> savedOptions = captor.getValue();
    assertThat(savedOptions).hasSize(productOptionRequests.size());

    assertThat(savedOptions.get(0).getColor())
        .isEqualTo(productOptionRequests.get(0).getColor());
  }

  @Test
  @DisplayName("똑같은 옵션 값이 존재하면 오류가 발생한다.")
  void createProductOption_should_throwException_when_productOption_is_duplicate() {
    productOptionRequests.add(createOptionRequest(ColorType.WHITE, SizeType.LARGE, 5));
    assertThatThrownBy(() -> productOptionService.createProductOption(product, productOptionRequests))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.DUPLICATE_PRODUCT_OPTION.getMessage());

    verify(productOptionRepository, never()).saveAll(anyList());

  }

  @Test
  @DisplayName("옵션이 정상적으로 수정된다.")
  void updateProductOption_should_update_productOption() {
    doNothing()
        .when(productOptionRepository)
        .softDeleteByProductId(product.getId());

    when(productOptionRepository.saveAll(anyList()))
        .thenAnswer(invocation -> {
          List<ProductOption> list = invocation.getArgument(0);
          return list;
        });

    when(productOptionMapper.toResponse(any(ProductOption.class)))
        .thenAnswer(invocation -> {
          ProductOption opt = invocation.getArgument(0);
          return ProductOptionResponse.builder()
              .id(opt.getId())
              .productId(opt.getProduct().getId())
              .color(opt.getColor())
              .size(opt.getSize())
              .stock(opt.getStock())
              .build();
        });

    List<ProductOptionResponse> results = productOptionService.updateProductOption(product, optionUpdateRequests);

    assertThat(results).hasSize(optionUpdateRequests.size());
    assertThat(results.get(0).getColor()).isEqualTo(optionUpdateRequests.get(0).getColor());
    assertThat(results.get(0).getSize()).isEqualTo(optionUpdateRequests.get(0).getSize());

    verify(productOptionRepository).softDeleteByProductId(product.getId());
    verify(productOptionRepository).saveAll(anyList());
    verify(productOptionMapper, times(2)).toResponse(any(ProductOption.class));

  }

}