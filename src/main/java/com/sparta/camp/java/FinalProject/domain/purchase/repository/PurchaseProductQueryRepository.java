package com.sparta.camp.java.FinalProject.domain.purchase.repository;

import static com.sparta.camp.java.FinalProject.domain.purchase.entity.QPurchase.purchase;
import static com.sparta.camp.java.FinalProject.domain.purchase.entity.QPurchaseProduct.purchaseProduct;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PurchaseProductQueryRepository {

  private final JPAQueryFactory queryFactory;

  public boolean findAllByProductIdAndActiveStatuses(Long productId, List<PurchaseStatus> activeStatuses) {

    Integer findOne = queryFactory
        .selectOne()
        .from(purchaseProduct)
        .join(purchaseProduct.purchase, purchase)
        .where(
            purchaseProduct.product.id.eq(productId),
            purchase.purchaseStatus.in(activeStatuses)
        )
        .fetchFirst();

    return findOne != null;
  }
}
