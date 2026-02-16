package com.sparta.camp.java.FinalProject.domain.purchase.repository;

import static com.sparta.camp.java.FinalProject.domain.purchase.entity.QPurchase.purchase;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSearchRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSummaryResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PurchaseQueryRepository {

  private final JPAQueryFactory queryFactory;

  private JPAQuery<PurchaseSummaryResponse> baseQuery() {
    return queryFactory
        .select(Projections.constructor(
            PurchaseSummaryResponse.class,
            purchase.id,
            purchase.purchaseNo,
            purchase.totalPrice,
            purchase.purchaseStatus,
            purchase.createdAt
        ))
        .from(purchase);
  }

  public List<PurchaseSummaryResponse> findAll(PurchaseSearchRequest searchRequest,
      PaginationRequest paginationRequest) {
    return baseQuery()
        .where(
            this.purchaseNoEq(searchRequest.getPurchaseNo()),
            this.userEmailEq(searchRequest.getUserEmail()),
            this.statusEq(searchRequest.getPurchaseStatus()),
            this.dateGoe(searchRequest.getStartDate()),
            this.dateLt(searchRequest.getEndDate())
        )
        .orderBy(
            purchase.createdAt.desc()
        )
        .offset(calculateOffset(paginationRequest))
        .limit(paginationRequest.getSize())
        .fetch();
  }

  public List<PurchaseSummaryResponse> findAllByUserId(Long userId, PaginationRequest request) {
    return baseQuery()
        .where(purchase.user.id.eq(userId))
        .orderBy(
            purchase.createdAt.desc(),
            purchase.purchaseStatus.asc()
        )
        .offset(calculateOffset(request))
        .limit(request.getSize())
        .fetch();
  }

  public long countPurchases(PurchaseSearchRequest searchRequest) {
    return queryFactory
        .select(purchase.count())
        .from(purchase)
        .where(
            this.purchaseNoEq(searchRequest.getPurchaseNo()),
            this.userEmailEq(searchRequest.getUserEmail()),
            this.statusEq(searchRequest.getPurchaseStatus()),
            this.dateGoe(searchRequest.getStartDate()),
            this.dateLt(searchRequest.getEndDate())
        )
        .fetchOne();
  }

  public long countPurchasesByUserId(Long userId) {
    return queryFactory
        .select(purchase.count())
        .from(purchase)
        .where(
            purchase.user.id.eq(userId)
        )
        .fetchOne();
  }

  private BooleanExpression purchaseNoEq(String purchaseNo) {
    return purchaseNo != null ? purchase.purchaseNo.eq(purchaseNo) : null;
  }

  private BooleanExpression userEmailEq(String email) {
    return email != null ? purchase.user.email.eq(email) : null;
  }

  private BooleanExpression statusEq(PurchaseStatus status) {
    return status != null ? purchase.purchaseStatus.eq(status) : null;
  }

  private BooleanExpression dateGoe(LocalDate startDate) {
    return startDate != null ? purchase.createdAt.goe(startDate.atStartOfDay()) : null;
  }

  private BooleanExpression dateLt(LocalDate endDate) {
    return endDate != null ? purchase.createdAt.lt(endDate.plusDays(1).atStartOfDay()) : null;
  }

  private int calculateOffset(PaginationRequest request) {
    int page = request.getPage() != null ? request.getPage() : 1;
    int size = request.getSize() != null ? request.getSize() : 10;
    return size * Math.max(0, page - 1);
  }
}
