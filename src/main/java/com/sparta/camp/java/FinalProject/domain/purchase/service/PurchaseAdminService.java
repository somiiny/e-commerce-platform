package com.sparta.camp.java.FinalProject.domain.purchase.service;

import com.sparta.camp.java.FinalProject.common.enums.CreatorType;
import com.sparta.camp.java.FinalProject.common.enums.HistoryType;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.admin.entity.Admin;
import com.sparta.camp.java.FinalProject.domain.admin.repository.AdminRepository;
import com.sparta.camp.java.FinalProject.domain.history.entity.History;
import com.sparta.camp.java.FinalProject.domain.history.repository.HistoryRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseStatusUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseAdminService {

  private final AdminRepository adminRepository;
  private final PurchaseRepository purchaseRepository;
  private final HistoryRepository historyRepository;

  record HistoryItem(
      Purchase purchase,
      PurchaseStatus oldStatus,
      PurchaseStatus newStatus,
      String reason,
      Long adminId
  ) { }

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

  private Admin getAdminByEmail(String email) {
    return adminRepository.findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_ADMIN));
  }

  private void createHistory(HistoryItem item) {
    History history = History.builder()
        .historyType(HistoryType.PURCHASE)
        .purchase(item.purchase)
        .oldStatus(String.valueOf(item.oldStatus))
        .newStatus(String.valueOf(item.newStatus))
        .description(item.reason)
        .creatorType(CreatorType.ADMIN)
        .createdBy(item.adminId)
        .build();

    historyRepository.save(history);
  }

}
