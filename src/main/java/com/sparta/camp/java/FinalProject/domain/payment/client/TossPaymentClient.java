package com.sparta.camp.java.FinalProject.domain.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.camp.java.FinalProject.common.exception.PaymentException;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentCancelRequest;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentCancelResponse;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentConfirmRequest;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentConfirmResponse;
import com.sparta.camp.java.FinalProject.domain.payment.dto.PaymentErrorResponse;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TossPaymentClient implements PaymentClient {

  private final ObjectMapper objectMapper;

  @Value("${payment.secret-key}")
  private String secretKey;

  @Override
  public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) throws Exception {
    return sendRequest(
        "https://api.tosspayments.com/v1/payments/confirm",
        request,
        PaymentConfirmResponse.class
    );
  }

  @Override
  public PaymentCancelResponse cancelPayment(PaymentCancelRequest request) throws Exception {
    return sendRequest(
        "https://api.tosspayments.com/v1/payments/" + request.getPaymentKey() + "/cancel",
        request,
        PaymentCancelResponse.class
    );
  }

  private <T> T sendRequest(String urlString, Object requestBody, Class<T> responseType)
      throws Exception {

    HttpURLConnection connection = null;

    try {
      String auth = Base64.getEncoder()
          .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

      URL url = new URL(urlString);
      connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Basic " + auth);
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);

      try (OutputStream os = connection.getOutputStream()) {
        objectMapper.writeValue(os, requestBody);
      }

      int statusCode = connection.getResponseCode();
      boolean success = statusCode >= 200 && statusCode < 300;

      InputStream responseStream =
          success ? connection.getInputStream() : connection.getErrorStream();

      if (responseStream == null) {
        throw new PaymentException(
            "PAYMENT_API_ERROR",
            "Empty response from payment provider"
        );
      }

      try (Reader reader =
          new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {

        if (!success) {
          PaymentErrorResponse errorResponse =
              objectMapper.readValue(reader, PaymentErrorResponse.class);

          throw new PaymentException(
              errorResponse.getCode(),
              errorResponse.getMessage()
          );
        }
        return objectMapper.readValue(reader, responseType);
      }
    } catch (PaymentException e) {
      throw e;
    } catch (Exception e) {
      throw new PaymentException("PAYMENT_API_ERROR", e.getMessage());
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}
