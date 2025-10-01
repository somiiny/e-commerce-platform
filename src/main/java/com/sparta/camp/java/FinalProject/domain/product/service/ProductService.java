package com.sparta.camp.java.FinalProject.domain.product.service;

import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductImageResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductSearchRequest;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductImage;
import com.sparta.camp.java.FinalProject.domain.product.mapper.ProductImageMapper;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductImageRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductQueryRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductQueryRepository productQueryRepository;
  private final ProductImageRepository productImageRepository;

  private final ProductImageMapper productImageMapper;

  public PaginationResponse<ProductResponse> getAllProducts(ProductSearchRequest searchRequest,
      PaginationRequest pageRequest) {

    List<Product> products = productQueryRepository.findProducts(searchRequest, pageRequest);
    if (products.isEmpty()) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT);
    }

    List<ProductResponse> responseList = products.stream().map(product -> {
      List<ProductImageResponse> images = product.getProductImageList().stream()
          .filter(pi -> pi.getDeletedAt() == null)
          .map(productImageMapper::toResponse)
          .toList();

      return ProductResponse.builder()
          .id(product.getId())
          .categoryId(product.getCategory().getId())
          .name(product.getName())
          .price(product.getPrice())
          .stock(product.getStock())
          .description(product.getDescription())
          .productImageResponseList(images)
          .createdAt(product.getCreatedAt())
          .updatedAt(product.getUpdatedAt())
          .build();
    }).toList();

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

    List<ProductImage> imageList = productImageRepository.findAllByProductId(productId);
    if(imageList.isEmpty()){
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT_IMAGE);
    }

    List<ProductImageResponse> imageResponseList = imageList
        .stream().map(productImageMapper::toResponse).toList();

    return ProductResponse.builder()
        .id(product.getId())
        .categoryId(product.getCategory().getId())
        .name(product.getName())
        .price(product.getPrice())
        .stock(product.getStock())
        .description(product.getDescription())
        .options(product.getOptions())
        .sellStatus(product.getSellStatus())
        .productImageResponseList(imageResponseList)
        .build();
  }

}
