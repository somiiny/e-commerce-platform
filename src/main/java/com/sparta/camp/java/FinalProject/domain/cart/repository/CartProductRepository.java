package com.sparta.camp.java.FinalProject.domain.cart.repository;

import com.sparta.camp.java.FinalProject.domain.cart.entity.CartProduct;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {

  @Query("SELECT cp FROM CartProduct cp WHERE cp.cart.id = :cartId AND cp.product.id = :productId "
      + "AND cp.deletedAt IS NULL")
  CartProduct findByCartAndProductId(Long cartId, Long productId);

  @Query("SELECT cp FROM CartProduct cp WHERE cp.cart.id = :cartId AND cp.deletedAt IS NULL")
  List<CartProduct> findAllByCartId (Long cartId);
}
