package com.sparta.camp.java.FinalProject.domain.purchase.repository;

import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

  @Query("SELECT p FROM Purchase p WHERE p.id = :purchaseId AND p.purchaseStatus = :purchaseStatus")
  Optional<Purchase> findByIdAndStatus(@Param("purchaseId") Long purchaseId,
      @Param("purchaseStatus") PurchaseStatus purchaseStatus);

  @Query("SELECT p FROM Purchase p WHERE p.user.id = :userId AND p.id = :purchaseId")
  Optional<Purchase> findByUserAndPurchaseId(@Param("userId") Long userId,
      @Param("purchaseId") Long purchaseId);
}
