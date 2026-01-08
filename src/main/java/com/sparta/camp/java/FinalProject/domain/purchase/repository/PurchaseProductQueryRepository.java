package com.sparta.camp.java.FinalProject.domain.purchase.repository;

import static com.sparta.camp.java.FinalProject.domain.purchase.entity.QPurchase.purchase;
import static com.sparta.camp.java.FinalProject.domain.purchase.entity.QPurchaseProduct.purchaseProduct;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseProductStatus;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PurchaseProductQueryRepository {

  private final JPAQueryFactory queryFactory;

  public boolean existsUndeletableProducts(Long productId) {

    Integer findOne = queryFactory
        .selectOne()
        .from(purchaseProduct)
        .join(purchaseProduct.purchase, purchase)
        .where(
            purchaseProduct.product.id.eq(productId),
            purchase.purchaseStatus.in(
                    PurchaseStatus.PURCHASE_CREATED,
                    PurchaseStatus.PURCHASE_PAID,
                    PurchaseStatus.PURCHASE_FULFILLING
            )
            .or(
                purchase.purchaseStatus.eq(PurchaseStatus.PARTIALLY_REFUNDED)
                    .and(purchaseProduct.status.ne(PurchaseProductStatus.CANCELED))
            )
        )
        .fetchFirst();

    return findOne != null;
  }
}
