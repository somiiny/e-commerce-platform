package com.sparta.camp.java.FinalProject.domain.cart.repository;

import com.sparta.camp.java.FinalProject.domain.cart.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

  @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.deletedAt IS NULL")
  Optional<Cart> findByUserId(Long userId);

}
