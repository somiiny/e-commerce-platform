package com.sparta.camp.java.FinalProject.domain.purchase.service;

import com.sparta.camp.java.FinalProject.common.enums.HistoryType;
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

  public void updatePurchaseStatus(String userName, PurchaseStatusUpdateRequest request) {

    User user = getUserByEmail(userName);

    Purchase purchase = purchaseRepository.findById(request.getPurchaseId())
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PURCHASE));
    if (purchase.getPurchaseStatus().equals(request.getStatus())) {
      throw new ServiceException(ServiceExceptionCode.DUPLICATE_STATUS);
    }

    History history = History.builder()
        .historyType(HistoryType.PURCHASE)
        .purchase(purchase)
        .oldStatus(String.valueOf(purchase.getPurchaseStatus()))
        .newStatus(String.valueOf(request.getStatus()))
        .description(request.getReason())
        .createdBy(user.getId())
        .build();

    purchase.setPurchaseStatus(request.getStatus());

    historyRepository.save(history);
  }

  private User getUserByEmail(String email) {
    return userRepository.findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_USER));
  }

}
