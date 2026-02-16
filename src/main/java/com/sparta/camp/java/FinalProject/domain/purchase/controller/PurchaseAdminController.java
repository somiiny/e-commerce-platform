package com.sparta.camp.java.FinalProject.domain.purchase.controller;

import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationResponse;
import com.sparta.camp.java.FinalProject.common.response.ApiResponse;
import com.sparta.camp.java.FinalProject.domain.auth.dto.CustomUserDetails;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSearchRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseStatusUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSummaryResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.service.PurchaseAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admins/purchases")
public class PurchaseAdminController {

  private final PurchaseAdminService purchaseAdminService;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ApiResponse<PaginationResponse<PurchaseSummaryResponse>> getPurchases(@ModelAttribute
      PurchaseSearchRequest searchRequest, @ModelAttribute PaginationRequest pageRequest) {
    return ApiResponse.success(purchaseAdminService.getPurchases(searchRequest, pageRequest));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{purchaseId}")
  public ApiResponse<PurchaseResponse> getPurchase(@PathVariable Long purchaseId) {
    return ApiResponse.success(purchaseAdminService.getPurchase(purchaseId));
  }

  @PutMapping
  public ApiResponse<Void> updatePurchaseStatus(@RequestBody PurchaseStatusUpdateRequest request,
      CustomUserDetails customUserDetails) {
    String userName = customUserDetails.getUsername();
    purchaseAdminService.updatePurchaseStatus(userName, request);
    return ApiResponse.success();
  }

  @DeleteMapping("/cancel/{purchaseId}")
  public ApiResponse<Void> cancelPurchase(CustomUserDetails userDetails,
      @PathVariable Long purchaseId) {
    String userName = userDetails.getUsername();
    purchaseAdminService.cancelPurchase(userName, purchaseId);
    return ApiResponse.success();
  }
}
