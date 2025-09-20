package com.sparta.camp.java.FinalProject.domain.category.repository;

import com.sparta.camp.java.FinalProject.domain.category.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")
  List<Category> findCategoryAll();

  @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")
  Optional<Category> findCategoryById(Long id);

  @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.deletedAt IS NULL")
  List<Category> findCategoryByParentId(Long parentId);
}
