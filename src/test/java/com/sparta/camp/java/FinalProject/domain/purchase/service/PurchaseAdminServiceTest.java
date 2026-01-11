package com.sparta.camp.java.FinalProject.domain.purchase.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.enums.Role;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.admin.entity.Admin;
import com.sparta.camp.java.FinalProject.domain.admin.repository.AdminRepository;
import com.sparta.camp.java.FinalProject.domain.history.entity.History;
import com.sparta.camp.java.FinalProject.domain.history.repository.HistoryRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseStatusUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PurchaseAdminServiceTest {

  @InjectMocks
  private PurchaseAdminService purchaseAdminService;

  @Mock
  private PurchaseRepository purchaseRepository;

  @Mock
  private AdminRepository adminRepository;

  @Mock
  private HistoryRepository historyRepository;

  private Admin admin;
  private Purchase purchase;
  private PurchaseStatusUpdateRequest request;

  @BeforeEach
  void setUp() {

    admin = Admin.builder()
        .email("test@email.com")
        .role(Role.ROLE_ADMIN)
        .build();
    ReflectionTestUtils.setField(admin, "id", 1L);

    purchase = Purchase.builder()
        .purchaseStatus(PurchaseStatus.PURCHASE_FULFILLING)
        .build();
    ReflectionTestUtils.setField(purchase, "id", 1L);

    request = new PurchaseStatusUpdateRequest();
    ReflectionTestUtils.setField(request, "purchaseId", 1L);
    ReflectionTestUtils.setField(request, "status", PurchaseStatus.PURCHASE_COMPLETED);
    ReflectionTestUtils.setField(request, "reason", "test");
  }

  @Test
  @DisplayName("주문 상태 수정이 정상적으로 수행된다.")
  void updatePurchaseStatus_should_update_status_successfully() {

    String oldStatus = String.valueOf(purchase.getPurchaseStatus());

    when(adminRepository.findByEmailAndDeletedAtIsNull(admin.getEmail()))
        .thenReturn(Optional.of(admin));
    when(purchaseRepository.findByPurchaseId(request.getPurchaseId()))
        .thenReturn(Optional.of(purchase));
    when(historyRepository.save(any(History.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    purchaseAdminService.updatePurchaseStatus(admin.getEmail(), request);

    ArgumentCaptor<History> historyCaptor = ArgumentCaptor.forClass(History.class);
    verify(historyRepository).save(historyCaptor.capture());

    History history = historyCaptor.getValue();
    assertThat(history.getPurchase()).isEqualTo(purchase);
    assertThat(history.getOldStatus()).isEqualTo(oldStatus);
    assertThat(history.getNewStatus()).isEqualTo(String.valueOf(request.getStatus()));
    assertThat(history.getCreatedBy()).isEqualTo(admin.getId());

    assertThat(purchase.getPurchaseStatus()).isEqualTo(request.getStatus());
    verify(adminRepository).findByEmailAndDeletedAtIsNull(admin.getEmail());
    verify(purchaseRepository).findByPurchaseId(request.getPurchaseId());

  }

  @Test
  @DisplayName("해당 관리자 계정이 존재하지 않는 경우 오류가 발생한다.")
  void updatePurchaseStatus_should_throwException_when_admin_is_not_exist() {

    when(adminRepository.findByEmailAndDeletedAtIsNull(admin.getEmail()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> purchaseAdminService.updatePurchaseStatus(admin.getEmail(), request))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_ADMIN.getMessage());

    verify(adminRepository).findByEmailAndDeletedAtIsNull(admin.getEmail());
    verifyNoMoreInteractions(adminRepository, purchaseRepository, historyRepository);
  }

  @Test
  @DisplayName("존재하지 않는 주문인 경우 오류가 발생한다.")
  void updatePurchaseStatus_should_throwException_when_purchase_is_not_exist() {

    when(adminRepository.findByEmailAndDeletedAtIsNull(admin.getEmail()))
        .thenReturn(Optional.of(admin));
    when(purchaseRepository.findByPurchaseId(request.getPurchaseId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> purchaseAdminService.updatePurchaseStatus(admin.getEmail(), request))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PURCHASE.getMessage());

    verify(adminRepository).findByEmailAndDeletedAtIsNull(admin.getEmail());
    verify(purchaseRepository).findByPurchaseId(request.getPurchaseId());

    verifyNoMoreInteractions(adminRepository, purchaseRepository, historyRepository);
  }

  @Test
  @DisplayName("기존 상태와 동일한 변경 요청인 경우 오류가 발생한다.")
  void updatePurchaseStatus_should_throwException_when_oldStatus_and_newStatus_are_same() {

    ReflectionTestUtils.setField(purchase, "purchaseStatus", request.getStatus());

    when(adminRepository.findByEmailAndDeletedAtIsNull(admin.getEmail()))
        .thenReturn(Optional.of(admin));
    when(purchaseRepository.findByPurchaseId(request.getPurchaseId()))
        .thenReturn(Optional.of(purchase));

    assertThatThrownBy(() -> purchaseAdminService.updatePurchaseStatus(admin.getEmail(), request))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.DUPLICATE_STATUS.getMessage());

    verify(adminRepository).findByEmailAndDeletedAtIsNull(admin.getEmail());
    verify(purchaseRepository).findByPurchaseId(request.getPurchaseId());
    verifyNoMoreInteractions(adminRepository, purchaseRepository, historyRepository);

  }

  @Test
  @DisplayName("변경할 수 없는 상태값인 경우 오류가 발생한다.")
  void updatePurchaseStatus_should_throwException_when_newStatus_is_invalid() {

    ReflectionTestUtils.setField(purchase, "purchaseStatus", PurchaseStatus.REFUNDED);

    when(adminRepository.findByEmailAndDeletedAtIsNull(admin.getEmail()))
      .thenReturn(Optional.of(admin));
    when(purchaseRepository.findByPurchaseId(request.getPurchaseId()))
      .thenReturn(Optional.of(purchase));

    assertThatThrownBy(() -> purchaseAdminService.updatePurchaseStatus(admin.getEmail(), request))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.INVALID_STATUS_TRANSITION.getMessage());

    verify(adminRepository).findByEmailAndDeletedAtIsNull(admin.getEmail());
    verify(purchaseRepository).findByPurchaseId(request.getPurchaseId());
    verifyNoMoreInteractions(adminRepository, purchaseRepository, historyRepository);

  }
}