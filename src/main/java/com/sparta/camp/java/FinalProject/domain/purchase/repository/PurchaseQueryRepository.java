package com.sparta.camp.java.FinalProject.domain.purchase.repository;

import static com.sparta.camp.java.FinalProject.domain.purchase.entity.QPurchase.purchase;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PurchaseQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<Purchase> findAllByUserId(Long userId, PaginationRequest request) {

    List<Purchase> purchaseList = queryFactory
        .selectFrom(purchase)
        .where(purchase.user.id.eq(userId))
        .orderBy(
            purchase.createdAt.desc(),
            purchase.purchaseStatus.asc()
        )
        .offset(calculateOffset(request))
        .limit(request.getSize())
        .fetch();

    purchaseList.forEach(p -> {
      p.getPurchaseProductList().size();
      p.getPurchaseProductList().forEach(pp ->
          pp.getProduct().getProductImageList().size()
      );
    });

    return purchaseList;
  }

  private int calculateOffset(PaginationRequest request) {
    int page = request.getPage() != null ? request.getPage() : 1;
    int size = request.getSize() != null ? request.getSize() : 10;
    return size * Math.max(0, page - 1);
  }
}
