package com.sparta.camp.java.FinalProject.domain.purchase.repository;

import static com.sparta.camp.java.FinalProject.domain.purchase.entity.QPurchase.purchase;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSummaryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PurchaseQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<PurchaseSummaryResponse> findAllByUserId(Long userId, PaginationRequest request) {
    return queryFactory
        .select(Projections.fields(PurchaseSummaryResponse.class,
                purchase.id,
                purchase.totalPrice,
                purchase.purchaseStatus.as("status"),
                purchase.createdAt))
        .from(purchase)
        .where(purchase.user.id.eq(userId))
        .orderBy(
            purchase.createdAt.desc(),
            purchase.purchaseStatus.asc()
        )
        .offset(calculateOffset(request))
        .limit(request.getSize())
        .fetch();
  }

  private int calculateOffset(PaginationRequest request) {
    int page = request.getPage() != null ? request.getPage() : 1;
    int size = request.getSize() != null ? request.getSize() : 10;
    return size * Math.max(0, page - 1);
  }
}
