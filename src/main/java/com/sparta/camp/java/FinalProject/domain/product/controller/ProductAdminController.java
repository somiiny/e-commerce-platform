package com.sparta.camp.java.FinalProject.domain.product.controller;

import com.sparta.camp.java.FinalProject.common.response.ApiResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductCreateRequest;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.product.service.ProductAdminService;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admins/products")
public class ProductAdminController {

  private final ProductAdminService productAdminService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Long> createProduct(@Valid @RequestBody ProductCreateRequest productCreateRequest)
      throws IOException {
    return ApiResponse.success(productAdminService.createProduct(productCreateRequest));
  }

  @PutMapping("/{productId}")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<ProductResponse> updateProduct(@PathVariable("productId") Long productId,
      @Valid @RequestBody ProductUpdateRequest productUpdateRequest)
      throws IOException {
    return ApiResponse.success(productAdminService.updateProduct(productId, productUpdateRequest));
  }

  @DeleteMapping("/{productId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ApiResponse<Void> deleteProduct(@PathVariable("productId") Long productId) {
    productAdminService.deleteProduct(productId);
    return ApiResponse.success();
  }

}
