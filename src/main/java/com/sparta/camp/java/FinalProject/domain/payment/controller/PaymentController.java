package com.sparta.camp.java.FinalProject.domain.payment.controller;

import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.common.response.ApiResponse;
import com.sparta.camp.java.FinalProject.domain.auth.dto.CustomUserDetails;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentCancelRequest;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentCancelResponse;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentConfirmRequest;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentConfirmResponse;
import com.sparta.camp.java.FinalProject.domain.payment.service.PaymentCacheService;
import com.sparta.camp.java.FinalProject.domain.payment.service.PaymentService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentCacheService paymentCacheService;

  @PostMapping("/cacheAmount")
  public ApiResponse<Void> cacheAmount(@RequestBody PaymentConfirmRequest request) {
    paymentCacheService.saveAmount(request.getPurchaseId(), request.getAmount());
    return ApiResponse.success();
  }

  @PostMapping("/validateAmount")
  public ApiResponse<Void> validateAmount(@RequestBody PaymentConfirmRequest request) {

    BigDecimal cachedAmount = paymentCacheService.getAmount(request.getPurchaseId());
    if (cachedAmount == null || cachedAmount.compareTo(request.getAmount()) != 0) {
      throw new ServiceException(ServiceExceptionCode.NOT_MATCH_PAYMENT_INFO);
    }

    paymentCacheService.removeAmount(request.getPurchaseId());

    return ApiResponse.success();
  }

  @PostMapping(value = "/confirm")
  public ApiResponse<PaymentConfirmResponse> confirmPayment(@RequestBody PaymentConfirmRequest request,
      CustomUserDetails userDetails) throws Exception {
    String userName = userDetails.getUsername();
    return ApiResponse.success(paymentService.confirmPayment(request, userName));
  }

  @PutMapping(value = "/cancel/{paymentId}")
  public ApiResponse<PaymentCancelResponse> cancelPayment(@PathVariable("paymentId") Long paymentId, PaymentCancelRequest request,
      CustomUserDetails userDetails) throws Exception {
    String userName = userDetails.getUsername();
    return ApiResponse.success(paymentService.cancelPayment(paymentId, request, userName));
  }

}
