package com.sparta.camp.java.FinalProject.domain.product.service;

import static java.util.stream.Collectors.groupingBy;

import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductImageResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductOptionResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductSearchRequest;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductImage;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductOption;
import com.sparta.camp.java.FinalProject.domain.product.mapper.ProductImageMapper;
import com.sparta.camp.java.FinalProject.domain.product.mapper.ProductMapper;
import com.sparta.camp.java.FinalProject.domain.product.mapper.ProductOptionMapper;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductImageRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductOptionRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductQueryRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductQueryRepository productQueryRepository;
  private final ProductOptionRepository productOptionRepository;
  private final ProductImageRepository productImageRepository;

  private final ProductMapper productMapper;
  private final ProductOptionMapper productOptionMapper;
  private final ProductImageMapper productImageMapper;

  public PaginationResponse<ProductResponse> getAllProducts(ProductSearchRequest searchRequest,
      PaginationRequest pageRequest) {

    List<Product> products = productQueryRepository.findProducts(searchRequest, pageRequest);

    if (products.isEmpty()) {
      long totalItems = productQueryRepository.countProducts(searchRequest);

      return PaginationResponse.<ProductResponse>builder()
          .paginationRequest(pageRequest)
          .totalItems(totalItems)
          .content(List.of())
          .build();
    }

    List<Long> productIds = products.stream()
        .map(Product::getId)
        .toList();

    List<ProductOption> options = productOptionRepository.findProductOptionInProductIds(productIds);
    Map<Long, List<ProductOption>> optionMap =
        options.stream().collect(groupingBy(po -> po.getProduct().getId()));

    List<ProductImage> images = productImageRepository.findProductImageInProductIds(productIds);
    Map<Long, List<ProductImage>> imageMap =
        images.stream().collect(groupingBy(img -> img.getProduct().getId()));

    List<ProductResponse> responseList = products.stream()
        .map(product -> {
          ProductResponse response = productMapper.toResponse(product);
          response.setProductOptions(convertToOptionResponse(optionMap.getOrDefault(product.getId(), List.of())));
          response.setProductImages(convertToImageResponse(imageMap.getOrDefault(product.getId(), List.of())));
          return response;
        })
        .toList();

    long totalItems = productQueryRepository.countProducts(searchRequest);

    return PaginationResponse.<ProductResponse>builder()
        .paginationRequest(pageRequest)
        .totalItems(totalItems)
        .content(responseList)
        .build();
  }

  public ProductResponse getProductById(Long productId){
    Product product = productRepository.findProductById(productId)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT));

    List<ProductOption> options = product.getProductOptions().stream()
        .filter(po -> po.getDeletedAt() == null)
        .toList();
    if (options.isEmpty()) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT_OPTIONS);
    }

    List<ProductImage> images = product.getProductImages().stream()
        .filter(pi -> pi.getDeletedAt() == null)
        .toList();
    if (images.isEmpty()) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT_IMAGE);
    }

    ProductResponse productResponse = productMapper.toResponse(product);
    productResponse.setProductOptions(convertToOptionResponse(options));
    productResponse.setProductImages(convertToImageResponse(images));

    return productResponse;
  }

  private List<ProductOptionResponse> convertToOptionResponse(List<ProductOption> productOptions) {
    return productOptions.stream()
        .map(productOptionMapper::toResponse)
        .toList();
  }

  private List<ProductImageResponse> convertToImageResponse(List<ProductImage> productImages){
    return productImages.stream()
        .map(productImageMapper::toResponse)
        .toList();
  }

}
