package com.sparta.camp.java.FinalProject.domain.cart.controller;

import com.sparta.camp.java.FinalProject.common.response.ApiResponse;
import com.sparta.camp.java.FinalProject.domain.auth.dto.CustomUserDetails;
import com.sparta.camp.java.FinalProject.domain.cart.dto.CartProductCreateRequest;
import com.sparta.camp.java.FinalProject.domain.cart.dto.CartProductUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.cart.dto.CartResponse;
import com.sparta.camp.java.FinalProject.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

  private final CartService cartService;

  @GetMapping
  public ApiResponse<CartResponse> getCartProducts (@AuthenticationPrincipal CustomUserDetails userDetail) {
    String userName = userDetail.getUsername();
    return ApiResponse.success(cartService.getCartProduct(userName));
  }

  @PostMapping
  public ApiResponse<Void> createCartProduct (@AuthenticationPrincipal CustomUserDetails userDetail,
      @RequestBody CartProductCreateRequest request) {
    String userName = userDetail.getUsername();
    cartService.createCartProduct(userName, request);
    return ApiResponse.success();
  }

  @PutMapping("/{cartProductId}")
  public ApiResponse<Void> updateCartProductQuantity (@PathVariable Long cartProductId,
      @RequestBody CartProductUpdateRequest request) {
    cartService.updateCartProductQuantity(cartProductId, request);
    return ApiResponse.success();
  }

  @DeleteMapping("/{cartProductId}")
  public ApiResponse<Void> deleteCartProduct(@PathVariable Long cartProductId) {
    cartService.deleteCartProduct(cartProductId);
    return ApiResponse.success();
  }

}
