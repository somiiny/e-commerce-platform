package com.sparta.camp.java.FinalProject.domain.user.repository;

import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("SELECT u FROM User u WHERE u.id = :id and u.deletedAt IS NULL")
  Optional<User> findByIdAndDeletedAtIsNull(Long id);

  @Query("SELECT u FROM User u WHERE u.email = :email and u.deletedAt IS NULL")
  Optional<User> findByEmailAndDeletedAtIsNull(String email);

}
