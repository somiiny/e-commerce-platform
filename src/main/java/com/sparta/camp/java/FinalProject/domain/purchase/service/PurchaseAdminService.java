package com.sparta.camp.java.FinalProject.domain.purchase.service;

import com.sparta.camp.java.FinalProject.common.enums.CreatorType;
import com.sparta.camp.java.FinalProject.common.enums.HistoryType;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseProductStatus;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationResponse;
import com.sparta.camp.java.FinalProject.domain.admin.entity.Admin;
import com.sparta.camp.java.FinalProject.domain.admin.repository.AdminRepository;
import com.sparta.camp.java.FinalProject.domain.history.entity.History;
import com.sparta.camp.java.FinalProject.domain.history.repository.HistoryRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSearchRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseStatusUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSummaryResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.PurchaseProduct;
import com.sparta.camp.java.FinalProject.domain.purchase.mapper.PurchaseMapper;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseQueryRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseAdminService {

  private final PurchaseMapper purchaseMapper;

  private final AdminRepository adminRepository;
  private final PurchaseRepository purchaseRepository;
  private final PurchaseQueryRepository purchaseQueryRepository;
  private final HistoryRepository historyRepository;

  record HistoryItem(
      Purchase purchase,
      PurchaseStatus oldStatus,
      PurchaseStatus newStatus,
      String reason,
      Long adminId
  ) { }

  @Transactional(readOnly = true)
  public PaginationResponse<PurchaseSummaryResponse> getPurchases(PurchaseSearchRequest searchRequest,
      PaginationRequest paginationRequest) {

    List<PurchaseSummaryResponse> purchases = purchaseQueryRepository.findAll(searchRequest, paginationRequest);

    long totalCounts = purchaseQueryRepository.countPurchases(searchRequest);

    return PaginationResponse.<PurchaseSummaryResponse>builder()
        .paginationRequest(paginationRequest)
        .totalItems(totalCounts)
        .content(purchases)
        .build();
  }

  @Transactional(readOnly = true)
  public PurchaseResponse getPurchase(Long purchaseId) {
    Purchase purchase = getPurchaseById(purchaseId);
    return purchaseMapper.toResponse(purchase);
  }

  @Transactional
  public void updatePurchaseStatus(String userName, PurchaseStatusUpdateRequest request) {

    Admin admin = getAdminByEmail(userName);

    Purchase purchase = purchaseRepository.findByPurchaseId(request.getPurchaseId())
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PURCHASE));

    PurchaseStatus oldStatus = purchase.getPurchaseStatus();
    if (purchase.getPurchaseStatus().equals(request.getStatus())) {
      throw new ServiceException(ServiceExceptionCode.DUPLICATE_PURCHASE_STATUS);
    } else if (!oldStatus.canTransitionTo(request.getStatus())) {
      throw new ServiceException(ServiceExceptionCode.INVALID_STATUS_TRANSITION);
    }

    purchase.setPurchaseStatus(request.getStatus());

    createHistory(new HistoryItem(purchase, oldStatus, request.getStatus(), request.getReason(), admin.getId()));
  }

  @Transactional
  public void cancelPurchase(String userName, Long purchaseId) {

    Admin admin = getAdminByEmail(userName);
    Purchase purchase = getPurchaseById(purchaseId);

    if (!purchase.isCancelable()) {
      throw new ServiceException(ServiceExceptionCode.INVALID_PURCHASE_STATUS);
    }

    PurchaseStatus oldStatus = purchase.getPurchaseStatus();
    purchase.setPurchaseStatus(PurchaseStatus.PURCHASE_CANCELED);
    for (PurchaseProduct purchaseProduct : purchase.getPurchaseProductList()) {
      purchaseProduct.setStatus(PurchaseProductStatus.CANCELED);
    }

    createHistory(new HistoryItem(purchase, oldStatus, PurchaseStatus.PURCHASE_CANCELED,
        "주문취소", admin.getId()));
  }

  private Admin getAdminByEmail(String email) {
    return adminRepository.findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_ADMIN));
  }

  private Purchase getPurchaseById(Long purchaseId) {
    return purchaseRepository.findByPurchaseId(purchaseId)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PURCHASE));
  }

  private void createHistory(HistoryItem item) {
    History history = History.builder()
        .historyType(HistoryType.PURCHASE)
        .purchase(item.purchase())
        .oldStatus(String.valueOf(item.oldStatus()))
        .newStatus(String.valueOf(item.newStatus()))
        .description(item.reason())
        .creatorType(CreatorType.ADMIN)
        .createdBy(item.adminId)
        .build();

    historyRepository.save(history);
  }

}
