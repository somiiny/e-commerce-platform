package com.sparta.camp.java.FinalProject.domain.payment.event;

import com.sparta.camp.java.FinalProject.domain.payment.service.PaymentCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

  private final PaymentCacheService paymentCacheService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePaymentCompleted(PaymentCompletedEvent event) {
    paymentCacheService.removeAmount(event.purchaseId());
  }
}
