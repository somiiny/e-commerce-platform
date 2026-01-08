package com.sparta.camp.java.FinalProject.domain.purchase.repository;

import com.sparta.camp.java.FinalProject.domain.purchase.entity.PurchaseProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseProductRepository extends JpaRepository<PurchaseProduct, Long> {

}
