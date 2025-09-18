package com.sparta.camp.java.FinalProject.domain.admin.repository;

import com.sparta.camp.java.FinalProject.domain.admin.entity.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

  @Query("SELECT a FROM Admin a WHERE a.id = :id and a.deletedAt IS NULL")
  Optional<Admin> findByIdAndDeletedAtIsNull(Long id);

  @Query("SELECT a FROM Admin a WHERE a.email = :email and a.deletedAt IS NULL")
  Optional<Admin> findByEmailAndDeletedAtIsNull(String email);
}
