package com.sparta.camp.java.FinalProject.domain.payment.service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentCacheService {

  private final RedisTemplate<String, BigDecimal> redisTemplate;

  public void saveAmount(Long purchaseId, BigDecimal amount) {
    redisTemplate.opsForValue().set("payment:" + purchaseId, amount, 10, TimeUnit.MINUTES);
  }

  public BigDecimal getAmount(Long purchaseId) {
    return redisTemplate.opsForValue().get("payment:" + purchaseId);
  }

  public void removeAmount(Long purchaseId) {
    redisTemplate.delete("payment:" + purchaseId);
  }

}
