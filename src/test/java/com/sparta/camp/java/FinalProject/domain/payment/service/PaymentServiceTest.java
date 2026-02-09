package com.sparta.camp.java.FinalProject.domain.payment.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sparta.camp.java.FinalProject.common.enums.CancelType;
import com.sparta.camp.java.FinalProject.common.enums.PaymentStatus;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseProductStatus;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.history.entity.History;
import com.sparta.camp.java.FinalProject.domain.history.repository.HistoryRepository;
import com.sparta.camp.java.FinalProject.domain.payment.client.PaymentClient;
import com.sparta.camp.java.FinalProject.domain.payment.dto.CancelProductDto;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentCancelRequest;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentCancelResponse;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentConfirmRequest;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentConfirmResponse;
import com.sparta.camp.java.FinalProject.domain.payment.entity.Payment;
import com.sparta.camp.java.FinalProject.domain.payment.event.PaymentCompletedEvent;
import com.sparta.camp.java.FinalProject.domain.payment.repository.PaymentRepository;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductOption;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductOptionRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.PurchaseProduct;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseProductRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseRepository;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @InjectMocks
  private PaymentService paymentService;

  @Mock
  private ProductOptionRepository productOptionRepository;

  @Mock
  private PurchaseRepository purchaseRepository;

  @Mock
  private PurchaseProductRepository purchaseProductRepository;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private HistoryRepository historyRepository;

  @Mock
  private PaymentClient paymentClient;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  private PaymentConfirmRequest paymentConfirmRequest;
  private PaymentCancelRequest paymentCancelRequest;
  private PaymentConfirmResponse paymentConfirmResponse;
  private PaymentCancelResponse paymentCancelResponse;

  @BeforeEach
  void setUp() {
    paymentConfirmRequest = PaymentConfirmRequest.builder()
        .purchaseId(1L)
        .paymentKey("paymentKey")
        .amount(BigDecimal.valueOf(100000))
        .build();

    paymentConfirmResponse = PaymentConfirmResponse.builder()
        .paymentKey(paymentConfirmRequest.getPaymentKey())
        .method("card")
        .amount(BigDecimal.valueOf(100000))
        .status(PaymentStatus.DONE)
        .build();

    paymentCancelRequest = PaymentCancelRequest.builder()
        .cancelType(CancelType.ALL)
        .amount(BigDecimal.valueOf(300000))
        .paymentKey("paymentKey")
        .build();

    paymentCancelResponse = PaymentCancelResponse.builder()
        .paymentKey(paymentCancelRequest.getPaymentKey())
        .purchaseId(1L)
        .status(PaymentStatus.CANCELLED)
        .amount(paymentCancelRequest.getAmount())
        .build();
  }

  private Purchase createPurchase(Long purchaseId, String email) {
    User user = User.builder()
        .email(email)
        .build();

    ProductOption option = ProductOption.builder()
        .stock(10)
        .build();
    ReflectionTestUtils.setField(option, "id", 1L);

    PurchaseProduct purchaseProduct = PurchaseProduct.builder()
        .quantity(1)
        .purchasedOption(option)
        .build();

    Purchase purchase = Purchase.builder()
        .user(user)
        .purchaseStatus(PurchaseStatus.PURCHASE_CREATED)
        .totalPrice(BigDecimal.valueOf(100000))
        .build();
    ReflectionTestUtils.setField(purchase, "id", purchaseId);
    ReflectionTestUtils.setField(purchase, "purchaseProductList", List.of(purchaseProduct));

    purchaseProduct.setPurchase(purchase);

    return purchase;
  }

  private Purchase createPaidPurchase(Long purchaseId, String email) {
    User user = User.builder()
        .email(email)
        .build();

    ProductOption option = ProductOption.builder()
        .stock(10)
        .build();
    ReflectionTestUtils.setField(option, "id", 1L);

    ProductOption option2 = ProductOption.builder()
        .stock(10)
        .build();
    ReflectionTestUtils.setField(option, "id", 2L);

    PurchaseProduct purchaseProduct = PurchaseProduct.builder()
        .quantity(10)
        .purchasedOption(option)
        .priceAtPurchase(BigDecimal.valueOf(10000))
        .build();
    ReflectionTestUtils.setField(purchaseProduct, "id", 1L);

    PurchaseProduct purchaseProduct2 = PurchaseProduct.builder()
        .quantity(10)
        .purchasedOption(option2)
        .priceAtPurchase(BigDecimal.valueOf(20000))
        .build();
    ReflectionTestUtils.setField(purchaseProduct2, "id", 2L);

    Purchase purchase = Purchase.builder()
        .user(user)
        .purchaseStatus(PurchaseStatus.PURCHASE_PAID)
        .totalPrice(BigDecimal.valueOf(300000))
        .build();
    ReflectionTestUtils.setField(purchase, "id", purchaseId);
    ReflectionTestUtils.setField(purchase, "purchaseProductList",
        List.of(purchaseProduct, purchaseProduct2));

    purchaseProduct.setPurchase(purchase);
    purchaseProduct2.setPurchase(purchase);

    return purchase;
  }

  @Test
  @DisplayName("결제 승인이 성공적으로 수행된다.")
  void confirmPayment_should_succeed() throws Exception {

    Long purchaseId = 1L;
    String email = "test@test.com";

    Purchase purchase = createPurchase(purchaseId, email);
    PurchaseProduct purchaseProduct = purchase.getPurchaseProductList().get(0);
    List<ProductOption> options = List.of(purchaseProduct.getPurchasedOption());
    Integer remainStock = options.get(0).getStock() - purchaseProduct.getQuantity();

    when(purchaseRepository.findByIdAndPurchaseStatus(
        paymentConfirmRequest.getPurchaseId(),
        PurchaseStatus.PURCHASE_CREATED))
        .thenReturn(Optional.of(purchase));

    when(productOptionRepository.findByIdForUpdate(anyList()))
        .thenReturn(options);

    when(paymentClient.confirmPayment(paymentConfirmRequest)).thenReturn(paymentConfirmResponse);

    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(historyRepository.save(any(History.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PaymentConfirmResponse result = paymentService.confirmPayment(paymentConfirmRequest, email, false);

    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);

    verify(paymentRepository).save(captor.capture());

    Payment savedPayment = captor.getValue();
    assertThat(savedPayment.getAmount()).isEqualTo(BigDecimal.valueOf(100000));
    assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.DONE);

    assertThat(result.getStatus())
        .isEqualTo(PaymentStatus.DONE);

    assertThat(purchase.getPurchaseStatus())
        .isEqualTo(PurchaseStatus.PURCHASE_PAID);

    assertThat(purchaseProduct.getStatus())
        .isEqualTo(PurchaseProductStatus.PAID);

    assertThat(options.get(0).getStock())
        .isEqualTo(remainStock);

    verify(purchaseRepository).findByIdAndPurchaseStatus(paymentConfirmRequest.getPurchaseId(),
        PurchaseStatus.PURCHASE_CREATED);
    verify(productOptionRepository, times(2)).findByIdForUpdate(anyList());
    verify(paymentClient).confirmPayment(paymentConfirmRequest);
    verify(historyRepository).save(any(History.class));
    verify(eventPublisher).publishEvent(any(PaymentCompletedEvent.class));

  }

  @Test
  @DisplayName("해당 주문이 존재하지 않는 경우 오류가 발생한다.")
  void confirmPayment_should_throwException_when_purchase_not_found() {

    String email = "test@test.com";

    when(purchaseRepository.findByIdAndPurchaseStatus(
        paymentConfirmRequest.getPurchaseId(),
        PurchaseStatus.PURCHASE_CREATED))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.confirmPayment(paymentConfirmRequest, email, false))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PURCHASE.getMessage());

    verify(purchaseRepository)
        .findByIdAndPurchaseStatus(paymentConfirmRequest.getPurchaseId(), PurchaseStatus.PURCHASE_CREATED);
    verifyNoMoreInteractions(purchaseRepository, paymentClient, productOptionRepository,
        historyRepository, eventPublisher);

  }

  @Test
  @DisplayName("주문시 요청한 금액과 결제시 금액이 일치하지 않는 경우 오류가 발생한다.")
  void confirmPayment_should_throwException_when_amount_is_not_match() {
    Long purchaseId = 1L;
    String email = "test@test.com";

    Purchase purchase = createPurchase(purchaseId, email);
    ReflectionTestUtils.setField(paymentConfirmRequest, "amount", BigDecimal.valueOf(200000));

    when(purchaseRepository.findByIdAndPurchaseStatus(
        paymentConfirmRequest.getPurchaseId(),
        PurchaseStatus.PURCHASE_CREATED))
        .thenReturn(Optional.of(purchase));

    assertThatThrownBy(() -> paymentService.confirmPayment(paymentConfirmRequest, email, false))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_MATCH_PAYMENT_INFO.getMessage());

    verify(purchaseRepository)
        .findByIdAndPurchaseStatus(paymentConfirmRequest.getPurchaseId(), PurchaseStatus.PURCHASE_CREATED);
    verifyNoMoreInteractions(purchaseRepository, paymentClient, productOptionRepository,
        historyRepository, eventPublisher);
  }

  @Test
  @DisplayName("권한이 없는 사용자가 결제 요청한 경우 오류가 발생한다.")
  void confirmPayment_should_throwException_when_user_is_not_permitted() {
    Long purchaseId = 1L;
    String email = "test@test.com";

    Purchase purchase = createPurchase(purchaseId, email);

    when(purchaseRepository.findByIdAndPurchaseStatus(
        paymentConfirmRequest.getPurchaseId(),
        PurchaseStatus.PURCHASE_CREATED))
        .thenReturn(Optional.of(purchase));

    assertThatThrownBy(() -> paymentService.confirmPayment(paymentConfirmRequest,
        "test2@test.com",
        false))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_PERMIT_ACCESS.getMessage());

    verify(purchaseRepository)
        .findByIdAndPurchaseStatus(paymentConfirmRequest.getPurchaseId(), PurchaseStatus.PURCHASE_CREATED);
    verifyNoMoreInteractions(purchaseRepository, paymentClient, productOptionRepository,
        historyRepository, eventPublisher);
  }

  @Test
  @DisplayName("관리자인 경우 요청 권한 검증을 통과한다.")
  void confirmPayment_should_succeed_when_requester_is_admin() throws Exception {
    Long purchaseId = 1L;

    Purchase purchase = createPurchase(purchaseId, "test@test.com");
    PurchaseProduct purchaseProduct = purchase.getPurchaseProductList().get(0);
    List<ProductOption> options = List.of(purchaseProduct.getPurchasedOption());
    Integer remainStock = options.get(0).getStock() - purchaseProduct.getQuantity();

    when(purchaseRepository.findByIdAndPurchaseStatus(
        paymentConfirmRequest.getPurchaseId(),
        PurchaseStatus.PURCHASE_CREATED))
        .thenReturn(Optional.of(purchase));
    when(productOptionRepository.findByIdForUpdate(anyList()))
        .thenReturn(options);
    when(paymentClient.confirmPayment(paymentConfirmRequest))
        .thenReturn(paymentConfirmResponse);

    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(historyRepository.save(any(History.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PaymentConfirmResponse result =
        paymentService.confirmPayment(paymentConfirmRequest, "admin@test.com", true);

    assertThat(result.getStatus())
        .isEqualTo(PaymentStatus.DONE);

    assertThat(purchase.getPurchaseStatus())
        .isEqualTo(PurchaseStatus.PURCHASE_PAID);

    assertThat(purchaseProduct.getStatus())
        .isEqualTo(PurchaseProductStatus.PAID);

    assertThat(options.get(0).getStock())
        .isEqualTo(remainStock);

    verify(purchaseRepository).findByIdAndPurchaseStatus(paymentConfirmRequest.getPurchaseId(),
        PurchaseStatus.PURCHASE_CREATED);
    verify(productOptionRepository, times(2)).findByIdForUpdate(anyList());
    verify(paymentClient).confirmPayment(paymentConfirmRequest);
    verify(historyRepository).save(any(History.class));
    verify(eventPublisher).publishEvent(any(PaymentCompletedEvent.class));
  }

  @Test
  @DisplayName("재고보다 주문 수량이 많은 경우 오류가 발생한다.")
  void confirmPayment_should_throwException_when_stock_is_insufficient() {
    Long purchaseId = 1L;
    String email = "test@test.com";

    Purchase purchase = createPurchase(purchaseId, email);
    PurchaseProduct pp = purchase.getPurchaseProductList().get(0);
    List<ProductOption> options = List.of(pp.getPurchasedOption());
    ReflectionTestUtils.setField(options.get(0), "stock", 0);
    ReflectionTestUtils.setField(pp, "quantity", 1);

    when(purchaseRepository.findByIdAndPurchaseStatus(
        purchaseId,
        PurchaseStatus.PURCHASE_CREATED))
        .thenReturn(Optional.of(purchase));

    when(productOptionRepository.findByIdForUpdate(anyList()))
        .thenReturn(options);

    assertThatThrownBy(() ->
        paymentService.confirmPayment(paymentConfirmRequest, email, false)
    )
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.INSUFFICIENT_STOCK.getMessage());


    verify(purchaseRepository).findByIdAndPurchaseStatus(paymentConfirmRequest.getPurchaseId(),
        PurchaseStatus.PURCHASE_CREATED);
    verify(productOptionRepository).findByIdForUpdate(anyList());

    verifyNoMoreInteractions(purchaseRepository, paymentClient, productOptionRepository,
        historyRepository, eventPublisher);
  }

  @Test
  @DisplayName("결제 취소 승인이 성공적으로 수행된다.")
  void cancelPayment_should_succeed_when_full_refund() throws Exception {
    Long paymentId = 1L;
    String purchaserEmail = "user@test.com";

    Purchase purchase = createPaidPurchase(1L, purchaserEmail);
    List<ProductOption> options = List.of(
        purchase.getPurchaseProductList().get(0).getPurchasedOption(),
        purchase.getPurchaseProductList().get(1).getPurchasedOption());

    Payment payment = Payment.builder()
        .purchase(purchase)
        .amount(BigDecimal.valueOf(300000))
        .status(PaymentStatus.DONE)
        .build();
    ReflectionTestUtils.setField(payment, "id", paymentId);

    when(paymentRepository.findById(paymentId))
        .thenReturn(Optional.of(payment));

    when(paymentClient.cancelPayment(paymentCancelRequest))
        .thenReturn(paymentCancelResponse);

    when(productOptionRepository.findByIdForUpdate(anyList()))
        .thenReturn(options);

    when(historyRepository.save(any(History.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PaymentCancelResponse result =
        paymentService.cancelPayment(paymentId, paymentCancelRequest, purchaserEmail, false);

    assertThat(result).isNotNull();

    assertThat(payment.getStatus())
        .isEqualTo(PaymentStatus.CANCELLED);

    assertThat(purchase.getPurchaseStatus())
        .isEqualTo(PurchaseStatus.REFUNDED);

    purchase.getPurchaseProductList().forEach(pp -> {
      assertThat(pp.getStatus()).isEqualTo(PurchaseProductStatus.REFUNDED);
      assertThat(pp.getRemainingQuantity()).isZero();
    });

    assertThat(options.get(0).getStock()).isEqualTo(20);

    verify(paymentRepository).findById(paymentId);
    verify(paymentClient).cancelPayment(paymentCancelRequest);
    verify(productOptionRepository).findByIdForUpdate(anyList());
    verify(historyRepository).save(any(History.class));

  }

  @Test
  @DisplayName("부분 결제 취소 승인이 성공적으로 수행된다.")
  void cancelPayment_should_succeed_when_partial_refund() throws Exception {
    Long paymentId = 1L;
    String purchaserEmail = "user@test.com";

    Purchase purchase = createPaidPurchase(1L, purchaserEmail);
    List<ProductOption> options = List.of(purchase.getPurchaseProductList().get(1).getPurchasedOption());
    PurchaseProduct cancelProduct = purchase.getPurchaseProductList().get(1);

    Payment payment = Payment.builder()
        .purchase(purchase)
        .amount(BigDecimal.valueOf(300000))
        .status(PaymentStatus.DONE)
        .build();
    ReflectionTestUtils.setField(payment, "id", paymentId);

    CancelProductDto cancelProductDto = new CancelProductDto();
    ReflectionTestUtils.setField(cancelProductDto, "purchaseProductId", cancelProduct.getId());
    ReflectionTestUtils.setField(cancelProductDto, "quantity", 5);

    PaymentCancelRequest request = PaymentCancelRequest.builder()
        .cancelType(CancelType.PARTIAL)
        .paymentKey("paymentKey")
        .cancelProducts(List.of(cancelProductDto))
        .build();

    PaymentCancelResponse response = PaymentCancelResponse.builder()
        .paymentKey(request.getPaymentKey())
        .purchaseId(purchase.getId())
        .status(PaymentStatus.PARTIAL_CANCELLED)
        .build();

    when(paymentRepository.findById(paymentId))
        .thenReturn(Optional.of(payment));

    when(purchaseProductRepository.findAllById(anyList()))
        .thenReturn(List.of(cancelProduct));

    when(paymentClient.cancelPayment(request))
        .thenReturn(response);

    when(productOptionRepository.findByIdForUpdate(anyList()))
        .thenReturn(options);

    when(historyRepository.save(any(History.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PaymentCancelResponse result =
        paymentService.cancelPayment(paymentId, request, purchaserEmail, false);

    assertThat(result).isNotNull();

    assertThat(payment.getStatus())
        .isEqualTo(PaymentStatus.PARTIAL_CANCELLED);

    assertThat(purchase.getPurchaseStatus())
        .isEqualTo(PurchaseStatus.PARTIALLY_REFUNDED);

    assertThat(cancelProduct.getStatus()).isEqualTo(PurchaseProductStatus.PARTIALLY_REFUNDED);
    assertThat(cancelProduct.getRemainingQuantity()).isEqualTo(5);

    assertThat(payment.getRefundedAmount())
        .isEqualByComparingTo(BigDecimal.valueOf(5).multiply(cancelProduct.getPriceAtPurchase()));

    assertThat(options.get(0).getStock()).isEqualTo(15);

    verify(paymentRepository).findById(paymentId);
    verify(purchaseProductRepository).findAllById(anyList());
    verify(productOptionRepository).findByIdForUpdate(anyList());
    verify(paymentClient).cancelPayment(request);
    verify(historyRepository).save(any(History.class));

  }

  @Test
  @DisplayName("결제 취소 가능한 결제 상태가 아닌 경우 오류가 발생한다.")
  void cancelPayment_should_throwException_when_paymentStatus_is_not_valid() {
    Long paymentId = 1L;
    String purchaserEmail = "user@test.com";

    Purchase purchase = createPaidPurchase(1L, purchaserEmail);

    Payment payment = Payment.builder()
        .purchase(purchase)
        .status(PaymentStatus.CANCELLED)
        .build();
    ReflectionTestUtils.setField(payment, "id", paymentId);

    when(paymentRepository.findById(paymentId))
        .thenReturn(Optional.of(payment));

    assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, paymentCancelRequest, purchaserEmail, false))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.INVALID_PAYMENT_STATUS.getMessage());

    verify(paymentRepository).findById(paymentId);
    verifyNoMoreInteractions(paymentRepository, purchaseProductRepository, productOptionRepository,
        paymentClient, historyRepository);

  }

  @Test
  @DisplayName("취소 할 수 있는 갯수를 초과하는 요청인 경우 오류가 발생한다.")
  void cancelPayment_should_throwException_when_cancel_quantity_exceeds_remaining_stock() {
    Long paymentId = 1L;
    String purchaserEmail = "user@test.com";

    Purchase purchase = createPaidPurchase(1L, purchaserEmail);
    PurchaseProduct cancelProduct = purchase.getPurchaseProductList().get(1);
    ReflectionTestUtils.setField(cancelProduct, "refundedQuantity", 5);

    Payment payment = Payment.builder()
        .purchase(purchase)
        .status(PaymentStatus.DONE)
        .build();
    ReflectionTestUtils.setField(payment, "id", paymentId);

    CancelProductDto cancelProductDto = new CancelProductDto();
    ReflectionTestUtils.setField(cancelProductDto, "purchaseProductId", cancelProduct.getId());
    ReflectionTestUtils.setField(cancelProductDto, "quantity", 6);

    PaymentCancelRequest request = PaymentCancelRequest.builder()
        .cancelType(CancelType.PARTIAL)
        .paymentKey("paymentKey")
        .cancelProducts(List.of(cancelProductDto))
        .build();

    when(paymentRepository.findById(paymentId))
        .thenReturn(Optional.of(payment));

    when(purchaseProductRepository.findAllById(anyList()))
        .thenReturn(List.of(cancelProduct));

    assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, request, purchaserEmail, false))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.EXCEED_CANCEL_QUANTITY.getMessage());

    verify(paymentRepository).findById(paymentId);
    verify(purchaseProductRepository).findAllById(anyList());
    verifyNoMoreInteractions(paymentRepository, purchaseProductRepository, productOptionRepository,
        paymentClient, historyRepository);
  }

  @Test
  @DisplayName("취소 가능한 금액보다 큰 금액을 요청하는 경우 오류가 발생한다.")
  void cancelPayment_should_throwException_when_cancel_amount_is_not_valid() throws Exception {
    Long paymentId = 1L;
    String purchaserEmail = "user@test.com";

    Purchase purchase = createPaidPurchase(1L, purchaserEmail);
    PurchaseProduct cancelProduct = purchase.getPurchaseProductList().get(1);
    ReflectionTestUtils.setField(cancelProduct, "priceAtPurchase", BigDecimal.valueOf(50000));

    Payment payment = Payment.builder()
        .purchase(purchase)
        .amount(BigDecimal.valueOf(300000))
        .status(PaymentStatus.PARTIAL_CANCELLED)
        .build();
    ReflectionTestUtils.setField(payment, "id", paymentId);
    ReflectionTestUtils.setField(payment, "refundedAmount", BigDecimal.valueOf(100000));

    CancelProductDto cancelProductDto = new CancelProductDto();
    ReflectionTestUtils.setField(cancelProductDto, "purchaseProductId", cancelProduct.getId());
    ReflectionTestUtils.setField(cancelProductDto, "quantity", 6);

    PaymentCancelRequest request = PaymentCancelRequest.builder()
        .cancelType(CancelType.PARTIAL)
        .paymentKey("paymentKey")
        .cancelProducts(List.of(cancelProductDto))
        .build();

    when(paymentRepository.findById(paymentId))
        .thenReturn(Optional.of(payment));

    when(purchaseProductRepository.findAllById(anyList()))
        .thenReturn(List.of(cancelProduct));

    assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, request, purchaserEmail, false))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.EXCEEDS_PAYMENT_AMOUNT.getMessage());

    verify(paymentRepository).findById(paymentId);
    verify(purchaseProductRepository).findAllById(anyList());
    verify(paymentClient, never()).cancelPayment(any());
    verify(productOptionRepository, never()).findByIdForUpdate(any());
    verify(historyRepository, never()).save(any());

  }

  @Test
  @DisplayName("취소 요청 금액과 서버 계산 금액이 일치하지 않는 경우 오류가 발생한다.")
  void cancelPayment_should_throwException_when_cancel_amount_is_not_match() throws Exception {
    Long paymentId = 1L;
    String purchaserEmail = "user@test.com";

    Purchase purchase = createPaidPurchase(1L, purchaserEmail);

    Payment payment = Payment.builder()
        .purchase(purchase)
        .amount(BigDecimal.valueOf(300000))
        .status(PaymentStatus.DONE)
        .build();
    ReflectionTestUtils.setField(payment, "id", paymentId);

    PaymentCancelRequest request = PaymentCancelRequest.builder()
        .cancelType(CancelType.ALL)
        .paymentKey("paymentKey")
        .amount(BigDecimal.valueOf(250000))
        .build();

    when(paymentRepository.findById(paymentId))
        .thenReturn(Optional.of(payment));

    assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, request, purchaserEmail, false))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_MATCH_PAYMENT_INFO.getMessage());

    verify(paymentRepository).findById(paymentId);
    verify(purchaseProductRepository, never()).findAllById(anyList());
    verify(paymentClient, never()).cancelPayment(any());
    verify(productOptionRepository, never()).findByIdForUpdate(any());
    verify(historyRepository, never()).save(any());
  }

}