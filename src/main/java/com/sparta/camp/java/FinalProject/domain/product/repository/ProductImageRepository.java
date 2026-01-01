package com.sparta.camp.java.FinalProject.domain.product.repository;

import com.sparta.camp.java.FinalProject.domain.product.entity.ProductImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

  @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId AND pi.deletedAt IS NULL")
  List<ProductImage> findAllByProductId(@Param("productId") Long productId);

  @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id IN :productIds AND pi.deletedAt IS NULL")
  List<ProductImage> findProductImageInProductIds(@Param("productIds") List<Long> productIds);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE ProductImage i SET i.deletedAt = CURRENT_TIMESTAMP WHERE i.product.id = :productId")
  void softDeleteByProductId(@Param("productId") Long productId);
}
