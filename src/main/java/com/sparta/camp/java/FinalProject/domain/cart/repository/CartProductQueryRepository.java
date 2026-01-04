package com.sparta.camp.java.FinalProject.domain.cart.repository;

import static com.sparta.camp.java.FinalProject.domain.cart.entity.QCartProduct.cartProduct;
import static com.sparta.camp.java.FinalProject.domain.product.entity.QProduct.product;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.camp.java.FinalProject.domain.cart.entity.CartProduct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CartProductQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<CartProduct> findAll(Long cartId) {
    return queryFactory.selectFrom(cartProduct)
        .join(cartProduct.product, product).fetchJoin()
        .where(cartProduct.cart.id.eq(cartId),
            cartProduct.deletedAt.isNull(),
            product.deletedAt.isNull()
        )
        .fetch();
  }

}
