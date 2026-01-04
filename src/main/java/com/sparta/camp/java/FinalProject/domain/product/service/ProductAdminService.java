package com.sparta.camp.java.FinalProject.domain.product.service;

import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.category.entity.Category;
import com.sparta.camp.java.FinalProject.domain.category.repository.CategoryRepository;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductCreateRequest;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductImageResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductOptionResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductImageRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductOptionRepository;
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

  private final ProductImageService productImageService;
  private final ProductOptionService productOptionService;

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final ProductImageRepository productImageRepository;
  private final PurchaseProductQueryRepository purchaseProductQueryRepository;
  private final ProductOptionRepository productOptionRepository;

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
        .sellStatus(productCreateRequest.getSellStatus())
        .build();

    productRepository.save(newProduct);

    productOptionService.createProductOption(newProduct, productCreateRequest.getOptions());
    productImageService.createProductImages(newProduct, productCreateRequest.getImages());

    return newProduct.getId();
  }

  public ProductResponse updateProduct(Long productId, ProductUpdateRequest productUpdateRequest)
      throws IOException {

    Product product = this.getProductById(productId);
    Category category = this.getCategoryById(productUpdateRequest.getCategoryId());

    if (productRepository.findProductByIdAndName(productId, productUpdateRequest.getName()).isPresent()) {
      throw new ServiceException(ServiceExceptionCode.DUPLICATE_PRODUCT_NAME);
    }

    product.setCategory(category);
    product.setName(productUpdateRequest.getName());
    product.setPrice(productUpdateRequest.getPrice());
    product.setDescription(productUpdateRequest.getDescription());
    product.setSellStatus(productUpdateRequest.getSellStatus());

    List<ProductOptionResponse> optionResponseList = productOptionService.updateProductOption(product, productUpdateRequest.getOptions());
    List<ProductImageResponse> updateImageList = productImageService.updateProductImages(product, productUpdateRequest.getImages());

    return ProductResponse.builder()
        .id(productId)
        .categoryId(product.getCategory().getId())
        .name(product.getName())
        .price(product.getPrice())
        .description(product.getDescription())
        .sellStatus(product.getSellStatus())
        .productOptions(optionResponseList)
        .productImages(updateImageList)
        .build();
  }

  public void deleteProduct(Long productId) {

    boolean isExist = purchaseProductQueryRepository.existsUndeletableProducts(productId);
    if (isExist) {
      throw new ServiceException(ServiceExceptionCode.CANNOT_DELETE_PRODUCT);
    }

    Product product = this.getProductById(productId);
    product.setDeletedAt(LocalDateTime.now());

    productOptionRepository.softDeleteByProductId(product.getId());
    productImageRepository.softDeleteByProductId(product.getId());
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
