package com.sparta.camp.java.FinalProject.domain.payment.client;

import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentCancelRequest;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentCancelResponse;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentConfirmRequest;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentConfirmResponse;

public interface PaymentClient {

  PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) throws Exception;

  PaymentCancelResponse cancelPayment(PaymentCancelRequest request) throws Exception;
}
