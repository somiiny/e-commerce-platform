package com.sparta.camp.java.FinalProject.domain.cart.repository;

import com.sparta.camp.java.FinalProject.domain.cart.entity.CartProduct;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {

  @Query("SELECT cp FROM CartProduct cp WHERE cp.cart.id = :cartId AND cp.product.id = :productId "
      + "AND cp.deletedAt IS NULL")
  Optional<CartProduct> findByCartAndProductId(@Param("cartId") Long cartId, @Param("productId")Long productId);

  @Query("SELECT cp FROM CartProduct cp WHERE cp.id = :cartProductId AND cp.deletedAt IS NULL")
  Optional<CartProduct> findByIdAndDeletedAtIsNull(@Param("cartProductId") Long cartProductId);

  @Query("SELECT cp FROM CartProduct cp WHERE cp.cart.id = :cartId "
      + "AND cp.product.id = :productId "
      + "AND cp.option.id = :optionId "
      + "AND cp.deletedAt IS NULL ")
  CartProduct findExistingCartProduct(@Param("cartId") Long cartId, @Param("productId") Long productId, @Param("optionId") Long optionId);

  @Query("SELECT cp FROM CartProduct cp WHERE cp.cart.id = :cartId AND cp.deletedAt IS NULL")
  List<CartProduct> findAllByCartId (@Param("cartId") Long cartId);
}
