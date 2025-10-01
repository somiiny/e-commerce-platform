package com.sparta.camp.java.FinalProject.domain.product.repository;

import static com.sparta.camp.java.FinalProject.domain.category.entity.QCategory.category;
import static com.sparta.camp.java.FinalProject.domain.product.entity.QProduct.product;
import static com.sparta.camp.java.FinalProject.domain.product.entity.QProductImage.productImage;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductSearchRequest;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.QProduct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<Product> findProducts(ProductSearchRequest searchRequest,
      PaginationRequest pageRequest) {

    return queryFactory
        .selectFrom(product)
        .join(product.category, category).fetchJoin()
        .where(
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
        .join(product.category, category).fetchJoin()
        .where(
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
    boolean isAscending = "ASC".equalsIgnoreCase(request.getSortDirection());

    return switch (request.getSortType()) {
      case "name" -> isAscending ? product.name.asc() : product.name.desc();
      case "price" -> isAscending ? product.price.asc() : product.price.desc();
      case "sellStatus" -> isAscending ? product.sellStatus.asc() : product.sellStatus.desc();
      default -> isAscending ? product.createdAt.asc() : product.createdAt.desc();
    };
  }

}
