package com.sparta.camp.java.FinalProject.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.camp.java.FinalProject.common.enums.CancelType;
import com.sparta.camp.java.FinalProject.common.enums.HistoryType;
import com.sparta.camp.java.FinalProject.common.enums.PaymentStatus;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.enums.Role;
import com.sparta.camp.java.FinalProject.common.exception.PaymentException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.history.entity.History;
import com.sparta.camp.java.FinalProject.domain.history.repository.HistoryRepository;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentCancelRequest;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentCancelResponse;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentConfirmRequest;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentConfirmResponse;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentErrorResponse;
import com.sparta.camp.java.FinalProject.domain.payment.entity.Payment;
import com.sparta.camp.java.FinalProject.domain.payment.repository.PaymentRepository;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseRepository;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

  private final PurchaseRepository purchaseRepository;

  private final ObjectMapper objectMapper;
  private final PaymentRepository paymentRepository;
  private final HistoryRepository historyRepository;
  private final ProductRepository productRepository;

  @Value("${payment.secret-key}")
  private String secretKey;

  private <T> T sendPaymentRequest(String urlString, Object requestBody, Class<T> responseType)
      throws Exception {

    String auth = Base64.getEncoder()
        .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

    HttpURLConnection connection = null;
    try {
      URL url = new URL(urlString);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Basic " + auth);
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);

      try (OutputStream os = connection.getOutputStream()) {
        objectMapper.writeValue(os, requestBody);
      }

      int code = connection.getResponseCode();
      try (InputStream responseStream = code == 200 ? connection.getInputStream() : connection.getErrorStream();
          Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {

        if (code != 200) {
          PaymentErrorResponse errorResponse = objectMapper.readValue(reader, PaymentErrorResponse.class);
          throw new PaymentException(errorResponse.getCode(), errorResponse.getMessage());
        }

        return objectMapper.readValue(reader, responseType);
      }
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request, String userName)
      throws Exception {

    Purchase purchase = purchaseRepository.findByIdAndStatus(request.getPurchaseId(), PurchaseStatus.ORDER_PLACED)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PURCHASE));

    if(!purchase.getUser().getEmail().equals(userName)) {
      throw new ServiceException(ServiceExceptionCode.NOT_PERMIT_ACCESS);
    }

    if (purchase.getTotalPrice().compareTo(request.getAmount()) != 0) {
      throw new ServiceException(ServiceExceptionCode.NOT_MATCH_PAYMENT_INFO);
    }


    PaymentConfirmResponse response = sendPaymentRequest(
        "https://api.tosspayments.com/v1/payments/confirm",
        request,
        PaymentConfirmResponse.class
    );

    String oldStatus = String.valueOf(purchase.getPurchaseStatus());
    purchase.setPurchaseStatus(PurchaseStatus.PAYMENT_COMPLETED);

    historyRepository.save(convertToHistory(purchase, null, oldStatus));
    paymentRepository.save(convertToPayment(purchase, response));

    return response;
  }

  public PaymentCancelResponse cancelPayment(Long paymentId, PaymentCancelRequest request, String userName)
      throws Exception {

    Purchase purchase = purchaseRepository.findByIdAndStatus(request.getPurchaseId(), PurchaseStatus.PAYMENT_COMPLETED)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PURCHASE));

    if(!purchase.getUser().getEmail().equals(userName) && !purchase.getUser().getRole().equals(Role.ROLE_ADMIN)) {
      throw new ServiceException(ServiceExceptionCode.NOT_PERMIT_ACCESS);
    }

    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PAYMENT));

    String url = "https://api.tosspayments.com/v1/payments/" + request.getPaymentKey() + "/cancel";
    PaymentCancelResponse response = sendPaymentRequest(url, request, PaymentCancelResponse.class);

    String purchaseOldStatus = String.valueOf(purchase.getPurchaseStatus());
    String paymentOldStatus = String.valueOf(payment.getStatus());

    if (request.getCancelType() == CancelType.ALL) {
      purchase.setPurchaseStatus(PurchaseStatus.PAYMENT_CANCELLED);
      payment.setStatus(PaymentStatus.CANCELLED);
    } else {
      purchase.setPurchaseStatus(PurchaseStatus.PAYMENT_PARTIAL_CANCELLED);
      payment.setStatus(PaymentStatus.PARTIAL_CANCELLED);
    }

    purchase.setRefundedAmount(response.getAmount());
    payment.setAmount(response.getAmount());
    restoreProductStock(purchase, request);

    historyRepository.save(convertToHistory(purchase, null, purchaseOldStatus));
    historyRepository.save(convertToHistory(purchase, payment, paymentOldStatus));

    return response;
  }

  private History convertToHistory(Purchase purchase, Payment payment, String oldStatus) {
    return History.builder()
        .historyType(payment != null ? HistoryType.PAYMENT_STATUS_CHANGE : HistoryType.PURCHASE_STATUS_CHANGE)
        .purchase(purchase)
        .payment(payment)
        .oldStatus(String.valueOf(oldStatus))
        .newStatus(String.valueOf(payment != null ? payment.getStatus() : purchase.getPurchaseStatus()))
        .createdBy(purchase.getUser().getId())
        .build();
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

  private void restoreProductStock(Purchase purchase, PaymentCancelRequest request) {

    List<Long> productIds;
    if (request.getCancelType() == CancelType.ALL) {
      productIds = purchase.getPurchaseProductList().stream()
          .map(pp -> pp.getProduct().getId())
          .toList();
    } else {
      productIds = request.getCancelProductIds();
    }

    List<Product> products = productRepository.findAllByIn(productIds);
    if (products.size() != productIds.size()) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT);
    }

    Map<Long, Product> productMap = products.stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    purchase.getPurchaseProductList().stream()
        .filter(pp -> productIds.contains(pp.getProduct().getId()))
        .forEach(pp -> {
          Product product = productMap.get(pp.getProduct().getId());
          product.increaseProductStock(
              pp.getOptions().getColor(),
              pp.getOptions().getSize(),
              pp.getQuantity()
          );
        });
  }
}
