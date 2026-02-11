package com.sparta.camp.java.FinalProject.domain.purchase.controller;

import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.common.response.ApiResponse;
import com.sparta.camp.java.FinalProject.domain.auth.dto.CustomUserDetails;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.DirectPurchaseCreateRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseCreateRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSummaryResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.service.PurchaseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchases")
public class PurchaseController {

  private final PurchaseService purchaseService;

  @GetMapping
  public ApiResponse<List<PurchaseSummaryResponse>> getPurchases(CustomUserDetails userDetails,
      @ModelAttribute PaginationRequest pageRequest) {
    String userName = userDetails.getUsername();
    return ApiResponse.success(purchaseService.getPurchases(userName, pageRequest));
  }

  @GetMapping("/{purchaseId}")
  public ApiResponse<PurchaseResponse> getPurchase(@PathVariable Long purchaseId,
      CustomUserDetails userDetails) {
    String userName = userDetails.getUsername();
    return ApiResponse.success(purchaseService.getPurchase(userName, purchaseId));
  }

  @PostMapping("/cart")
  public ApiResponse<PurchaseResponse> createPurchaseFromCart(CustomUserDetails userDetails,
      @RequestBody PurchaseCreateRequest request) {
    String userName = userDetails.getUsername();
    return ApiResponse.success(purchaseService.createPurchaseFromCart(userName, request));
  }

  @PostMapping("/direct")
  public ApiResponse<PurchaseResponse> createPurchaseDirect(CustomUserDetails userDetails,
      @RequestBody DirectPurchaseCreateRequest request) {
    String userName = userDetails.getUsername();
    return ApiResponse.success(purchaseService.createPurchaseDirect(userName, request));
  }

  @DeleteMapping("/cancel/{purchaseId}")
  public ApiResponse<Void> cancelPurchase(CustomUserDetails userDetails,
      @PathVariable Long purchaseId) {
    String userName = userDetails.getUsername();
    purchaseService.cancelPurchase(userName, purchaseId);
    return ApiResponse.success();
  }


}
