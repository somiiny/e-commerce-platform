package com.sparta.camp.java.FinalProject.domain.product.repository;

import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.name = :name")
  Optional<Product> findProductByName(String name);

  @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.id = :id")
  Optional<Product> findProductById(Long id);

  @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.name = :name AND p.id = :id")
  Optional<Product> findProductByName(Long id, String name);

  @Query("SELECT p FROM Product p WHERE p.id IN :productIds AND p.deletedAt IS NULL")
  List<Product> findAllByIn(List<Long> productIds);

  @Query("SELECT count(p) > 0 FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL")
  boolean existsByCategoryId(Long categoryId);

}
