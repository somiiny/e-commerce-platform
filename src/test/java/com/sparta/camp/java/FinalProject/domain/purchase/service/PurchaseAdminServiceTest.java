package com.sparta.camp.java.FinalProject.domain.purchase.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.enums.Role;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationResponse;
import com.sparta.camp.java.FinalProject.domain.admin.entity.Admin;
import com.sparta.camp.java.FinalProject.domain.admin.repository.AdminRepository;
import com.sparta.camp.java.FinalProject.domain.history.entity.History;
import com.sparta.camp.java.FinalProject.domain.history.repository.HistoryRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSearchRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseStatusUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSummaryResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseQueryRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
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
  private PurchaseQueryRepository purchaseQueryRepository;

  @Mock
  private AdminRepository adminRepository;

  @Mock
  private HistoryRepository historyRepository;

  private Admin admin;
  private Purchase purchase;
  private List<PurchaseSummaryResponse> purchases;

  private PurchaseSearchRequest searchRequest;
  private PurchaseStatusUpdateRequest updateRequest;
  private PaginationRequest paginationRequest;

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

    purchases = createSummaryResponses();

    paginationRequest = new PaginationRequest();
    ReflectionTestUtils.setField(paginationRequest, "page", 0);
    ReflectionTestUtils.setField(paginationRequest, "size", 10);

    searchRequest = new PurchaseSearchRequest();

    updateRequest = new PurchaseStatusUpdateRequest();
    ReflectionTestUtils.setField(updateRequest, "purchaseId", 1L);
    ReflectionTestUtils.setField(updateRequest, "status", PurchaseStatus.PURCHASE_COMPLETED);
    ReflectionTestUtils.setField(updateRequest, "reason", "test");
  }

  private PurchaseSummaryResponse createSummaryResponse(Long id,
      String purchaseNo,
      PurchaseStatus status,
      LocalDateTime dateTime) {

    return PurchaseSummaryResponse.builder()
        .id(id)
        .purchaseNo(purchaseNo)
        .status(status)
        .createdAt(dateTime)
        .build();
  }

  private List<PurchaseSummaryResponse> createSummaryResponses() {
    LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);

    return IntStream.rangeClosed(1, 10)
        .mapToObj(i -> createSummaryResponse(
            (long) i,
            "PUR-" + i,
            PurchaseStatus.values()[i % PurchaseStatus.values().length],
            base.plusDays(i)
        ))
        .toList();
  }


  @Test
  @DisplayName("전체 주문 조회가 정상적으로 수행된다.")
  void getPurchases_should_return_purchaseList_successfully() {

    when(purchaseQueryRepository.findAll(searchRequest, paginationRequest))
      .thenReturn(purchases);
    when(purchaseQueryRepository.countPurchases(searchRequest))
      .thenReturn((long) purchases.size());

    PaginationResponse<PurchaseSummaryResponse> result =
        purchaseAdminService.getPurchases(searchRequest, paginationRequest);

    assertThat(result).isNotNull();
    assertThat(result.getTotalItems()).isEqualTo(purchases.size());
    assertThat(result.getCurrentPage())
        .isEqualTo(paginationRequest.getPage());
    assertThat(result.getContent().get(0).getPurchaseNo())
        .isEqualTo("PUR-1");

    verify(purchaseQueryRepository).findAll(searchRequest, paginationRequest);
    verify(purchaseQueryRepository).countPurchases(searchRequest);
    verifyNoMoreInteractions(purchaseQueryRepository);
  }

  @Test
  @DisplayName("검색결과 없는 경우")
  void getPurchases_should_return_emptyList_successfully() {

    when(purchaseQueryRepository.findAll(searchRequest, paginationRequest))
        .thenReturn(List.of());
    when(purchaseQueryRepository.countPurchases(searchRequest))
        .thenReturn(0L);

    PaginationResponse<PurchaseSummaryResponse> result =
        purchaseAdminService.getPurchases(searchRequest, paginationRequest);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalItems()).isEqualTo(0);
    assertThat(result.getCurrentPage())
        .isEqualTo(paginationRequest.getPage());

    verify(purchaseQueryRepository).findAll(searchRequest, paginationRequest);
    verify(purchaseQueryRepository).countPurchases(searchRequest);
    verifyNoMoreInteractions(purchaseQueryRepository);
  }

  @Test
  @DisplayName("주문 상태 수정이 정상적으로 수행된다.")
  void updatePurchaseStatus_should_update_status_successfully() {

    String oldStatus = String.valueOf(purchase.getPurchaseStatus());

    when(adminRepository.findByEmailAndDeletedAtIsNull(admin.getEmail()))
        .thenReturn(Optional.of(admin));
    when(purchaseRepository.findByPurchaseId(updateRequest.getPurchaseId()))
        .thenReturn(Optional.of(purchase));
    when(historyRepository.save(any(History.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    purchaseAdminService.updatePurchaseStatus(admin.getEmail(), updateRequest);

    ArgumentCaptor<History> historyCaptor = ArgumentCaptor.forClass(History.class);
    verify(historyRepository).save(historyCaptor.capture());

    History history = historyCaptor.getValue();
    assertThat(history.getPurchase()).isEqualTo(purchase);
    assertThat(history.getOldStatus()).isEqualTo(oldStatus);
    assertThat(history.getNewStatus()).isEqualTo(String.valueOf(updateRequest.getStatus()));
    assertThat(history.getCreatedBy()).isEqualTo(admin.getId());

    assertThat(purchase.getPurchaseStatus()).isEqualTo(updateRequest.getStatus());
    verify(adminRepository).findByEmailAndDeletedAtIsNull(admin.getEmail());
    verify(purchaseRepository).findByPurchaseId(updateRequest.getPurchaseId());

  }

  @Test
  @DisplayName("해당 관리자 계정이 존재하지 않는 경우 오류가 발생한다.")
  void updatePurchaseStatus_should_throwException_when_admin_is_not_exist() {

    when(adminRepository.findByEmailAndDeletedAtIsNull(admin.getEmail()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> purchaseAdminService.updatePurchaseStatus(admin.getEmail(), updateRequest))
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
    when(purchaseRepository.findByPurchaseId(updateRequest.getPurchaseId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> purchaseAdminService.updatePurchaseStatus(admin.getEmail(), updateRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PURCHASE.getMessage());

    verify(adminRepository).findByEmailAndDeletedAtIsNull(admin.getEmail());
    verify(purchaseRepository).findByPurchaseId(updateRequest.getPurchaseId());

    verifyNoMoreInteractions(adminRepository, purchaseRepository, historyRepository);
  }

  @Test
  @DisplayName("기존 상태와 동일한 변경 요청인 경우 오류가 발생한다.")
  void updatePurchaseStatus_should_throwException_when_oldStatus_and_newStatus_are_same() {

    ReflectionTestUtils.setField(purchase, "purchaseStatus", updateRequest.getStatus());

    when(adminRepository.findByEmailAndDeletedAtIsNull(admin.getEmail()))
        .thenReturn(Optional.of(admin));
    when(purchaseRepository.findByPurchaseId(updateRequest.getPurchaseId()))
        .thenReturn(Optional.of(purchase));

    assertThatThrownBy(() -> purchaseAdminService.updatePurchaseStatus(admin.getEmail(), updateRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.DUPLICATE_PURCHASE_STATUS.getMessage());

    verify(adminRepository).findByEmailAndDeletedAtIsNull(admin.getEmail());
    verify(purchaseRepository).findByPurchaseId(updateRequest.getPurchaseId());
    verifyNoMoreInteractions(adminRepository, purchaseRepository, historyRepository);

  }

  @Test
  @DisplayName("변경할 수 없는 상태값인 경우 오류가 발생한다.")
  void updatePurchaseStatus_should_throwException_when_newStatus_is_invalid() {

    ReflectionTestUtils.setField(purchase, "purchaseStatus", PurchaseStatus.REFUNDED);

    when(adminRepository.findByEmailAndDeletedAtIsNull(admin.getEmail()))
      .thenReturn(Optional.of(admin));
    when(purchaseRepository.findByPurchaseId(updateRequest.getPurchaseId()))
      .thenReturn(Optional.of(purchase));

    assertThatThrownBy(() -> purchaseAdminService.updatePurchaseStatus(admin.getEmail(), updateRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.INVALID_STATUS_TRANSITION.getMessage());

    verify(adminRepository).findByEmailAndDeletedAtIsNull(admin.getEmail());
    verify(purchaseRepository).findByPurchaseId(updateRequest.getPurchaseId());
    verifyNoMoreInteractions(adminRepository, purchaseRepository, historyRepository);

  }

  @Test
  @DisplayName("주문 취소가 정상적으로 수행된다.")
  void cancelPurchase_should_cancel_purchase_successfully() {
    ReflectionTestUtils.setField(purchase, "purchaseStatus", PurchaseStatus.PURCHASE_CREATED);

    when(adminRepository.findByEmailAndDeletedAtIsNull(admin.getEmail()))
        .thenReturn(Optional.of(admin));
    when(purchaseRepository.findByPurchaseId(purchase.getId()))
        .thenReturn(Optional.of(purchase));
    when(historyRepository.save(any(History.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    purchaseAdminService.cancelPurchase(admin.getEmail(), purchase.getId());

    ArgumentCaptor<History> historyCaptor = ArgumentCaptor.forClass(History.class);
    verify(historyRepository).save(historyCaptor.capture());

    History history = historyCaptor.getValue();
    assertThat(history.getPurchase()).isEqualTo(purchase);
    assertThat(history.getOldStatus()).isEqualTo(PurchaseStatus.PURCHASE_CREATED.toString());
    assertThat(history.getNewStatus()).isEqualTo(PurchaseStatus.PURCHASE_CANCELED.toString());
    assertThat(history.getCreatedBy()).isEqualTo(admin.getId());

    assertThat(purchase.getPurchaseStatus()).isEqualTo(PurchaseStatus.PURCHASE_CANCELED);
    verify(adminRepository).findByEmailAndDeletedAtIsNull(admin.getEmail());
    verify(purchaseRepository).findByPurchaseId(updateRequest.getPurchaseId());

  }

  @Test
  @DisplayName("취소할 수 없는 주문 상태면 오류가 발생한다.")
  void cancelPurchase_should_throwException_when_purchase_status_is_invalid() {

    when(adminRepository.findByEmailAndDeletedAtIsNull(admin.getEmail()))
        .thenReturn(Optional.of(admin));
    when(purchaseRepository.findByPurchaseId(purchase.getId()))
        .thenReturn(Optional.of(purchase));

    assertThatThrownBy(() -> purchaseAdminService.cancelPurchase(admin.getEmail(), purchase.getId()))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.INVALID_PURCHASE_STATUS.getMessage());

    verify(adminRepository).findByEmailAndDeletedAtIsNull(admin.getEmail());
    verify(purchaseRepository).findByPurchaseId(purchase.getId());
    verify(historyRepository, never()).save(any(History.class));

  }
}