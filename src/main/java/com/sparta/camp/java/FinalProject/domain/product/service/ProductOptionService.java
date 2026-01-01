package com.sparta.camp.java.FinalProject.domain.product.service;

import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductOptionRequest;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductOptionResponse;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductOption;
import com.sparta.camp.java.FinalProject.domain.product.mapper.ProductOptionMapper;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductOptionRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductOptionService {

  private final ProductOptionRepository productOptionRepository;
  private final ProductOptionMapper productOptionMapper;

  public void createProductOption(Product product, List<ProductOptionRequest> productOptions) {

    validateDuplicateOptions(productOptions);

    List<ProductOption> productOptionList = productOptions.stream()
        .map(opt -> ProductOption.builder()
            .product(product)
            .color(opt.getColor())
            .size(opt.getSize())
            .stock(opt.getStock())
            .build())
        .toList();

    productOptionRepository.saveAll(productOptionList);
  }

  public List<ProductOptionResponse> updateProductOptionList (Product product, List<ProductOptionRequest> productOptions) {

    productOptionRepository.softDeleteByProductId(product.getId());

    validateDuplicateOptions(productOptions);

    List<ProductOption> saved = productOptionRepository.saveAll(
        productOptions.stream()
            .map(opt -> ProductOption.builder()
                .product(product)
                .color(opt.getColor())
                .size(opt.getSize())
                .stock(opt.getStock())
                .build())
            .toList()
    );

    return saved.stream()
        .map(productOptionMapper::toResponse)
        .toList();
  }

  private void validateDuplicateOptions(List<ProductOptionRequest> options) {

    Set<String> uniqueKeys = new HashSet<>();

    for (ProductOptionRequest opt : options) {
      String key = opt.getColor() + "_" + opt.getSize();

      if (!uniqueKeys.add(key)) {
        throw new ServiceException(ServiceExceptionCode.DUPLICATE_PRODUCT_OPTION);
      }
    }
  }

}
