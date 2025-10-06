package com.sparta.camp.java.FinalProject.domain.purchase.controller;

import com.sparta.camp.java.FinalProject.common.response.ApiResponse;
import com.sparta.camp.java.FinalProject.domain.auth.dto.CustomUserDetails;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseStatusUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.service.PurchaseAdminService;
import com.sparta.camp.java.FinalProject.domain.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admins/purchases")
public class PurchaseAdminController {

  private final PurchaseAdminService purchaseAdminService;

  @PutMapping
  public ApiResponse<Void> updatePurchaseStatus(@RequestBody PurchaseStatusUpdateRequest request,
      CustomUserDetails customUserDetails) {
    String userName = customUserDetails.getUsername();
    purchaseAdminService.updatePurchaseStatus(userName, request);
    return ApiResponse.success();
  }
}
