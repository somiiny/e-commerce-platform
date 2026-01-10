package com.sparta.camp.java.FinalProject.domain.purchase.service;

import com.sparta.camp.java.FinalProject.common.enums.HistoryType;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.history.entity.History;
import com.sparta.camp.java.FinalProject.domain.history.repository.HistoryRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseStatusUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseRepository;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import com.sparta.camp.java.FinalProject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseAdminService {

  private final PurchaseRepository purchaseRepository;
  private final UserRepository userRepository;
  private final HistoryRepository historyRepository;

  record HistoryItem(
      Purchase purchase,
      PurchaseStatus oldStatus,
      PurchaseStatus newStatus,
      String reason,
      Long userId
  ) { }

  public void updatePurchaseStatus(String userName, PurchaseStatusUpdateRequest request) {

    User user = getUserByEmail(userName);

    Purchase purchase = purchaseRepository.findByUserIdAndPurchaseId(user.getId(), request.getPurchaseId())
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PURCHASE));

    PurchaseStatus oldStatus = purchase.getPurchaseStatus();
    if (purchase.getPurchaseStatus().equals(request.getStatus())) {
      throw new ServiceException(ServiceExceptionCode.DUPLICATE_STATUS);
    } else if (!oldStatus.canTransitionTo(request.getStatus())) {
      throw new ServiceException(ServiceExceptionCode.INVALID_STATUS_TRANSITION);
    }

    purchase.setPurchaseStatus(request.getStatus());

    createHistory(new HistoryItem(purchase, oldStatus, request.getStatus(), request.getReason(), user.getId()));
  }

  private User getUserByEmail(String email) {
    return userRepository.findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_USER));
  }

  private void createHistory(HistoryItem item) {
    History history = History.builder()
        .historyType(HistoryType.PURCHASE)
        .purchase(item.purchase)
        .oldStatus(String.valueOf(item.oldStatus))
        .newStatus(String.valueOf(item.newStatus))
        .description(item.reason)
        .createdBy(item.userId)
        .build();

    historyRepository.save(history);
  }

}
