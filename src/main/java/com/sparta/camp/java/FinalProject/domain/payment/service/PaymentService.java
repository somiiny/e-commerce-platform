package com.sparta.camp.java.FinalProject.domain.payment.service;

import com.sparta.camp.java.FinalProject.common.enums.CancelType;
import com.sparta.camp.java.FinalProject.common.enums.HistoryType;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final ProductOptionRepository productOptionRepository;
  private final PurchaseRepository purchaseRepository;
  private final PurchaseProductRepository purchaseProductRepository;
  private final PaymentRepository paymentRepository;
  private final HistoryRepository historyRepository;

  private final PaymentClient paymentClient;
  private final ApplicationEventPublisher eventPublisher;

  public record CancelProductInfo(
      PurchaseProduct pp,
      Integer quantity
  ) {}

  public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request,
      String userName, boolean isAdmin)
      throws Exception {

    Purchase purchase = getValidatePurchase(request);
    validateRequesterPermission(purchase.getUser().getEmail(), userName, isAdmin);

    PaymentConfirmResponse response = paymentClient.confirmPayment(request);

    savePaymentResult(purchase, response);

    return response;
  }

  private Purchase getValidatePurchase(PaymentConfirmRequest request) {
    Purchase purchase = purchaseRepository.findByIdAndPurchaseStatus(request.getPurchaseId(),
            PurchaseStatus.PURCHASE_CREATED)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PURCHASE));

    if (purchase.getTotalPrice().compareTo(request.getAmount()) != 0) {
      throw new ServiceException(ServiceExceptionCode.NOT_MATCH_PAYMENT_INFO);
    }

    return purchase;
  }

  @Transactional
  protected void savePaymentResult(Purchase purchase, PaymentConfirmResponse response) {
    String oldStatus = String.valueOf(purchase.getPurchaseStatus());

    purchase.setPurchaseStatus(PurchaseStatus.PURCHASE_PAID);
    purchase.getPurchaseProductList().forEach(pp -> pp.setStatus(PurchaseProductStatus.PAID));

    decreaseStock(purchase.getPurchaseProductList());

    Payment newPayment = convertToPayment(purchase, response);
    paymentRepository.save(newPayment);

    saveHistory(newPayment, purchase, oldStatus);

    eventPublisher.publishEvent(
        new PaymentCompletedEvent(purchase.getId())
    );
  }

  private void decreaseStock(List<PurchaseProduct> purchaseProductList) {
    for (PurchaseProduct pp : purchaseProductList) {
      ProductOption option =
          productOptionRepository.findByIdForUpdate(
              pp.getPurchasedOption().getId()
          );
      option.decreaseStock(pp.getQuantity());
    }
  }

  private Payment convertToPayment(Purchase purchase, PaymentConfirmResponse response) {
    return Payment.builder()
        .purchase(purchase)
        .transactionId(response.getPaymentKey())
        .method(response.getMethod())
        .amount(response.getAmount())
        .status(response.getStatus())
        .paidAt(response.getApprovedAt())
        .build();
  }

  public PaymentCancelResponse cancelPayment(Long paymentId,
      PaymentCancelRequest request,
      String userName,
      boolean isAdmin) throws Exception {

    validateCancelType(request);

    Payment cancelPayment = getCancelablePayment(paymentId);
    Purchase cancelPurchase = cancelPayment.getPurchase();
    validateRequesterPermission(cancelPurchase.getUser().getEmail(), userName, isAdmin);

    List<CancelProductInfo> cancelItems =
        validateAndCollectCancelItems(cancelPurchase, request);

    BigDecimal cancelAmount = calculateCancelAmount(cancelItems);

    validateCancelAmount(cancelPayment, request, cancelAmount);

    PaymentCancelResponse response = paymentClient.cancelPayment(request);

    updatePaymentResult(cancelPayment, cancelPurchase, cancelItems, cancelAmount);

    return response;
  }

  private void validateCancelType(PaymentCancelRequest request) {
    if (request.getCancelType() == CancelType.ALL && request.getAmount() == null) {
      throw new ServiceException(ServiceExceptionCode.INVALID_CANCEL_REQUEST);
    }

    if (request.getCancelType()  == CancelType.PARTIAL &&
        (request.getCancelProducts() == null || request.getCancelProducts().isEmpty())) {
      throw new ServiceException(ServiceExceptionCode.INVALID_CANCEL_REQUEST);
    }
  }

  private Payment getCancelablePayment(Long id) {
    Payment payment = paymentRepository.findById(id)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PAYMENT));

    if (!payment.isCancelable()) {
      throw new ServiceException(ServiceExceptionCode.INVALID_PAYMENT_STATUS);
    }

    return payment;
  }

  private List<CancelProductInfo> validateAndCollectCancelItems(
      Purchase purchase,
      PaymentCancelRequest request
  ) {
    if (request.getCancelType() == CancelType.ALL) {
      return purchase.getPurchaseProductList().stream()
          .filter(pp -> pp.getRemainingQuantity() > 0)
          .map(pp -> new CancelProductInfo(pp, pp.getRemainingQuantity()))
          .toList();
    }

    return validateCancelItems(purchase, request.getCancelProducts());
  }

  private List<CancelProductInfo> validateCancelItems(
      Purchase purchase,
      List<CancelProductDto> cancelProducts
  ) {

    List<Long> purchaseProductIds = validateDuplicateCancelProductIds(cancelProducts);

    List<PurchaseProduct> purchaseProducts =
        purchaseProductRepository.findAllById(purchaseProductIds);

    if (purchaseProducts.size() != purchaseProductIds.size()) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_PURCHASE_PRODUCT);
    }

    Map<Long, PurchaseProduct> productMap = purchaseProducts.stream()
        .collect(Collectors.toMap(
            PurchaseProduct::getId,
            Function.identity()));

    List<CancelProductInfo> result = new ArrayList<>();

    for (CancelProductDto dto : cancelProducts) {
      PurchaseProduct pp = productMap.get(dto.getPurchaseProductId());

      if (!pp.getPurchase().getId().equals(purchase.getId())) {
        throw new ServiceException(ServiceExceptionCode.INVALID_CANCEL_REQUEST);
      }

      if (dto.getQuantity() > pp.getRemainingQuantity()) {
        throw new ServiceException(ServiceExceptionCode.EXCEED_CANCEL_QUANTITY);
      }

      result.add(new CancelProductInfo(pp, dto.getQuantity()));
    }

    return result;
  }

  private List<Long> validateDuplicateCancelProductIds(List<CancelProductDto> cancelProducts) {
    List<Long> purchaseProductIds = cancelProducts.stream()
        .map(CancelProductDto::getPurchaseProductId)
        .toList();

    Set<Long> uniqueIds = new HashSet<>(purchaseProductIds);

    if (uniqueIds.size() != purchaseProductIds.size()) {
      throw new ServiceException(ServiceExceptionCode.INVALID_CANCEL_REQUEST);
    }

    return purchaseProductIds;
  }

  private BigDecimal calculateCancelAmount(List<CancelProductInfo> validatedProducts) {
    BigDecimal total = BigDecimal.ZERO;

    for (CancelProductInfo dto : validatedProducts) {
      BigDecimal refundAmount = dto.pp().getPriceAtPurchase()
          .multiply(BigDecimal.valueOf(dto.quantity()));

      total = total.add(refundAmount);
    }

    return total;
  }

  private void validateCancelAmount(
      Payment payment,
      PaymentCancelRequest request,
      BigDecimal cancelAmount
  ) {

    BigDecimal remainingAmount = payment.getRemainingAmount();
    if (cancelAmount.compareTo(remainingAmount) > 0) {
      throw new ServiceException(ServiceExceptionCode.EXCEEDS_PAYMENT_AMOUNT);
    }

    if (request.getAmount() != null && request.getAmount().compareTo(cancelAmount) != 0) {
      throw new ServiceException(ServiceExceptionCode.NOT_MATCH_PAYMENT_INFO);
    }
  }

  @Transactional
  protected void updatePaymentResult(Payment payment,
      Purchase purchase,
      List<CancelProductInfo> cancelProductInfos,
      BigDecimal amount) {

    String oldStatus = String.valueOf(payment.getStatus());

    boolean isFullyCancelled = payment.getRemainingAmount()
        .subtract(amount)
        .compareTo(BigDecimal.ZERO) == 0;

    updatePayment(payment, amount, isFullyCancelled);
    updatePurchase(purchase, amount, isFullyCancelled);
    updatePurchaseProducts(cancelProductInfos);
    restoreStock(cancelProductInfos);
    saveHistory(payment, purchase, oldStatus);
  }

  private void updatePayment(Payment payment, BigDecimal amount, boolean isFullyCancelled) {
    payment.addRefundedAmount(amount);
    if (isFullyCancelled) {
      payment.setStatus(PaymentStatus.CANCELLED);
    } else {
      payment.setStatus(PaymentStatus.PARTIAL_CANCELLED);
    }
  }

  private void updatePurchase(Purchase purchase, BigDecimal amount, boolean isFullyCancelled) {
    purchase.addRefundedAmount(amount);
    if (isFullyCancelled) {
      purchase.setPurchaseStatus(PurchaseStatus.REFUNDED);
    } else {
      purchase.setPurchaseStatus(PurchaseStatus.PARTIALLY_REFUNDED);
    }
  }

  private void updatePurchaseProducts(List<CancelProductInfo> cancelProductInfos) {
    for (CancelProductInfo dto : cancelProductInfos) {
      PurchaseProduct pp = dto.pp();
      int qty = dto.quantity();

      pp.addRefundedQuantity(qty);

      if (pp.getRemainingQuantity() == 0) {
        pp.setStatus(PurchaseProductStatus.REFUNDED);
      } else {
        pp.setStatus(PurchaseProductStatus.PARTIALLY_REFUNDED);
      }
    }
  }

  private void restoreStock(List<CancelProductInfo> cancelProductInfos) {
    for (CancelProductInfo info : cancelProductInfos) {
      ProductOption option =
          productOptionRepository.findByIdForUpdate(info.pp().getPurchasedOption().getId());
      option.increaseStock(info.quantity());
    }
  }

  private void validateRequesterPermission(
      String purchaserEmail,
      String requesterEmail,
      boolean isAdmin
  ) {
    if (isAdmin) {
      return;
    }

    if (!purchaserEmail.equals(requesterEmail)) {
      throw new ServiceException(ServiceExceptionCode.NOT_PERMIT_ACCESS);
    }
  }

  private void saveHistory(Payment payment, Purchase purchase, String oldStatus) {
    History history = History.builder()
        .historyType(HistoryType.PAYMENT)
        .purchase(purchase)
        .payment(payment)
        .oldStatus(String.valueOf(oldStatus))
        .newStatus(String.valueOf(payment.getStatus()))
        .createdBy(purchase.getUser().getId())
        .build();
    historyRepository.save(history);
  }

}
