package com.sparta.camp.java.FinalProject.domain.product.service;

import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.category.entity.Category;
import com.sparta.camp.java.FinalProject.domain.category.repository.CategoryRepository;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductCreateRequest;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductImageResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductImageRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductAdminService {

  private final ProductImageService productImageService;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductImageRepository productImageRepository;

  public Long createProduct(ProductCreateRequest productCreateRequest) throws IOException {

    Category category = this.getCategoryById(productCreateRequest.getCategoryId());

    if (productRepository.findProductByName(productCreateRequest.getName()).isPresent()) {
      throw new ServiceException(ServiceExceptionCode.DUPLICATE_PRODUCT_NAME);
    }

    Product newProduct = Product.builder()
        .category(category)
        .name(productCreateRequest.getName())
        .price(productCreateRequest.getPrice())
        .stock(productCreateRequest.getStock())
        .description(productCreateRequest.getDescription())
        .options(productCreateRequest.getOptions())
        .sellStatus(productCreateRequest.getSellStatus())
        .build();

    productRepository.save(newProduct);
    productImageService.createProductImageList(newProduct, productCreateRequest.getImageList());

    return newProduct.getId();
  }

  public ProductResponse updateProduct(Long productId, ProductUpdateRequest productUpdateRequest)
      throws IOException {

    Product product = this.getProductById(productId);
    Category category = this.getCategoryById(productUpdateRequest.getCategoryId());

    if (productRepository.findProductByName(productId, productUpdateRequest.getName()).isPresent()) {
      throw new ServiceException(ServiceExceptionCode.DUPLICATE_PRODUCT_NAME);
    }

    product.setCategory(category);
    product.setName(productUpdateRequest.getName());
    product.setPrice(productUpdateRequest.getPrice());
    product.setStock(productUpdateRequest.getStock());
    product.setDescription(productUpdateRequest.getDescription());
    product.setOptions(productUpdateRequest.getOptions());
    product.setSellStatus(productUpdateRequest.getSellStatus());
    productRepository.save(product);

    List<ProductImageResponse> updateImageList = productImageService.updateProductImageList(product, productUpdateRequest.getImageList());

    return ProductResponse.builder()
        .id(productId)
        .categoryId(product.getCategory().getId())
        .name(product.getName())
        .price(product.getPrice())
        .stock(product.getStock())
        .description(product.getDescription())
        .options(product.getOptions())
        .sellStatus(product.getSellStatus())
        .productImageResponseList(updateImageList)
        .build();
  }

  public void deleteProduct(Long productId) {
    Product product = this.getProductById(productId);
    // 주문 완료 상태 여부

    LocalDateTime now = LocalDateTime.now();
    product.setDeletedAt(now);
    productRepository.save(product);
    productImageRepository.softDeleteByProductId(product.getId(), now);
  }

  private Category getCategoryById(Long categoryId) {
    return categoryRepository.findCategoryById(categoryId)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_CATEGORY));
  }

  private Product getProductById(Long productId) {
    return productRepository.findProductById(productId)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT));
  }

}
