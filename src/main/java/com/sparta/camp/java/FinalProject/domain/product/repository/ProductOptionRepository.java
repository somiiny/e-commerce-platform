package com.sparta.camp.java.FinalProject.domain.product.repository;

import com.sparta.camp.java.FinalProject.domain.product.entity.ProductOption;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

  @Query("SELECT po FROM ProductOption po WHERE po.id = :productOptionId AND po.deletedAt IS NULL")
  Optional<ProductOption> findByProductOptionId(@Param("productOptionId") Long productOptionId);

  @Query("SELECT po FROM ProductOption po "
      + "WHERE po.product.id = :productId "
      + "AND po.id = :productOptionId")
  Optional<ProductOption> findByIdAndProductId(
      @Param("productId") Long productId,
      @Param("productOptionId") Long productOptionId);

  @Query("SELECT po FROM ProductOption po WHERE po.product.id IN :productIds AND po.deletedAt IS NULL")
  List<ProductOption> findProductOptionInProductIds(@Param("productIds") List<Long> productIds);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE ProductOption po SET po.deletedAt = CURRENT_TIMESTAMP WHERE po.product.id = :productId")
  void softDeleteByProductId(@Param("productId") Long productId);

  @Query("SELECT po FROM ProductOption po WHERE po.id IN :optionId AND po.deletedAt IS NULL")
  List<ProductOption> findAllValidByIds(@Param("optionIds") List<Long> optionIds);
}
