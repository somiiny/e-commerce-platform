package com.sparta.camp.java.FinalProject.domain.product.service;

import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
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
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseProductQueryRepository;
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

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ProductImageRepository productImageRepository;
  private final PurchaseProductQueryRepository purchaseProductQueryRepository;

  public Long createProduct(ProductCreateRequest productCreateRequest) throws IOException {

    Category category = this.getCategoryById(productCreateRequest.getCategoryId());

    if (productRepository.findProductByName(productCreateRequest.getName()).isPresent()) {
      throw new ServiceException(ServiceExceptionCode.DUPLICATE_PRODUCT_NAME);
    }

    Product newProduct = Product.builder()
        .category(category)
        .name(productCreateRequest.getName())
        .price(productCreateRequest.getPrice())
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
    product.setDescription(productUpdateRequest.getDescription());
    product.setOptions(productUpdateRequest.getOptions());
    product.setSellStatus(productUpdateRequest.getSellStatus());

    List<ProductImageResponse> updateImageList = productImageService.updateProductImageList(product, productUpdateRequest.getImageList());

    return ProductResponse.builder()
        .id(productId)
        .categoryId(product.getCategory().getId())
        .name(product.getName())
        .price(product.getPrice())
        .description(product.getDescription())
        .options(product.getOptions())
        .sellStatus(product.getSellStatus())
        .productImageResponseList(updateImageList)
        .build();
  }

  public void deleteProduct(Long productId) {
    List<PurchaseStatus> activeStatuses = List.of(
        PurchaseStatus.ORDER_PLACED,
        PurchaseStatus.PAYMENT_COMPLETED,
        PurchaseStatus.SHIPPING_PENDING
    );

    boolean isExist = purchaseProductQueryRepository.findAllByProductIdAndActiveStatuses(productId, activeStatuses);
    if (isExist) {
      throw new ServiceException(ServiceExceptionCode.CANNOT_DELETE_PRODUCT);
    }

    Product product = this.getProductById(productId);

    LocalDateTime now = LocalDateTime.now();
    product.setDeletedAt(now);
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
