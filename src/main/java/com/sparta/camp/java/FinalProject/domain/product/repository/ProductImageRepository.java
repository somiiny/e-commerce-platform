package com.sparta.camp.java.FinalProject.domain.product.repository;

import com.sparta.camp.java.FinalProject.domain.product.entity.ProductImage;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

  @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId AND i.deletedAt IS null")
  List<ProductImage> findAllByProductId(Long productId);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE ProductImage i SET i.deletedAt = :now WHERE i.product.id = :productId")
  void softDeleteByProductId(Long productId, LocalDateTime now);
}
