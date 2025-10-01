package com.sparta.camp.java.FinalProject.domain.product.controller;

import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationResponse;
import com.sparta.camp.java.FinalProject.common.response.ApiResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductSearchRequest;
import com.sparta.camp.java.FinalProject.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

  private final ProductService productService;

  @GetMapping
  public ApiResponse<PaginationResponse<ProductResponse>> getAllProducts(@ModelAttribute ProductSearchRequest searchRequest,
      @ModelAttribute PaginationRequest pageRequest) {
    return ApiResponse.success(productService.getAllProducts(searchRequest, pageRequest));
  }

  @GetMapping("/{productId}")
  public ApiResponse<ProductResponse> getProductById(@PathVariable Long productId){
    return ApiResponse.success(productService.getProductById(productId));
  }

}
