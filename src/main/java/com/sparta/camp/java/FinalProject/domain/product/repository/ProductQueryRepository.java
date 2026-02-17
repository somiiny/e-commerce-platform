package com.sparta.camp.java.FinalProject.domain.product.repository;

import static com.sparta.camp.java.FinalProject.domain.category.entity.QCategory.category;
import static com.sparta.camp.java.FinalProject.domain.product.entity.QProduct.product;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductSearchRequest;
import com.sparta.camp.java.FinalProject.domain.product.entity.QProduct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<ProductResponse> findProducts(ProductSearchRequest searchRequest,
      PaginationRequest pageRequest) {

    return queryFactory
        .select(Projections.constructor(
            ProductResponse.class,
            product.id,
            product.category.id,
            product.name,
            product.price,
            product.description,
            product.sellStatus,
            product.createdAt,
            product.updatedAt
        ))
        .from(product)
        .join(product.category, category)
        .where(
            category.id.eq(searchRequest.getCategoryId()),
            product.deletedAt.isNull(),
            category.deletedAt.isNull(),
            this.findContainKeyword(searchRequest.getKeywordType(), searchRequest.getKeyword()),
            this.priceGoe(searchRequest.getMinPrice()),
            this.priceLoe(searchRequest.getMaxPrice())
        )
        .offset(this.calculateOffset(pageRequest))
        .limit(pageRequest.getSize())
        .orderBy(this.orderBySortType(product, searchRequest))
        .fetch();
  }

  public long countProducts(ProductSearchRequest searchRequest) {
    return queryFactory
        .select(product.count())
        .from(product)
        .join(product.category, category)
        .where(
            category.id.eq(searchRequest.getCategoryId()),
            product.deletedAt.isNull(),
            category.deletedAt.isNull(),
            this.findContainKeyword(searchRequest.getKeywordType(), searchRequest.getKeyword()),
            this.priceGoe(searchRequest.getMinPrice()),
            this.priceLoe(searchRequest.getMaxPrice())
        )
        .fetchOne();
  }

  private BooleanExpression findContainKeyword(String keywordType, String keyword) {
    if (!StringUtils.hasText(keywordType)) return null;

    return switch (keywordType) {
      case "category" -> categoryContains(keyword);
      case "name" -> nameContains(keyword);
      case "description" -> descriptionContains(keyword);
      default -> null;
    };
  }

  private BooleanExpression categoryContains(String categoryName) {
    return StringUtils.hasText(categoryName) ? category.name.containsIgnoreCase(categoryName) : null;
  }

  private BooleanExpression nameContains(String name) {
    return StringUtils.hasText(name) ? product.name.containsIgnoreCase(name) : null;
  }

  private BooleanExpression descriptionContains(String description) {
    return StringUtils.hasText(description) ? product.description.containsIgnoreCase(description) : null;
  }

  private BooleanExpression priceGoe(Integer minPrice) {
    return minPrice != null ? product.price.goe(minPrice) : null;
  }

  private BooleanExpression priceLoe(Integer maxPrice) {
    return maxPrice != null ? product.price.loe(maxPrice) : null;
  }

  private int calculateOffset(PaginationRequest request) {
    int page = request.getPage() != null ? request.getPage() : 1;
    int size = request.getSize() != null ? request.getSize() : 10;
    return size * Math.max(0, page - 1);
  }

  private OrderSpecifier<?> orderBySortType(QProduct product, ProductSearchRequest request) {
    String sortType = request.getSortType();
    String sortDirection = request.getSortDirection();

    if (!StringUtils.hasText(sortType)) {
      sortType = "createdAt";
    }

    boolean isAscending = "ASC".equalsIgnoreCase(sortDirection);

    return switch (sortType) {
      case "name" -> isAscending ? product.name.asc() : product.name.desc();
      case "price" -> isAscending ? product.price.asc() : product.price.desc();
      case "sellStatus" -> isAscending ? product.sellStatus.asc() : product.sellStatus.desc();
      case "createdAt" -> isAscending ? product.createdAt.asc() : product.createdAt.desc();
      default -> product.createdAt.desc();
    };
  }

}
