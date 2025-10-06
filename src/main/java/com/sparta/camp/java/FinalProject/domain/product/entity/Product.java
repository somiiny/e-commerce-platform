package com.sparta.camp.java.FinalProject.domain.product.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.product.converter.ProductOptionConverter;
import com.sparta.camp.java.FinalProject.common.enums.SellStatus;
import com.sparta.camp.java.FinalProject.domain.category.entity.Category;
import com.sparta.camp.java.FinalProject.domain.product.vo.ProductOption;
import com.sparta.camp.java.FinalProject.domain.product.vo.SizeOption;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  @JsonBackReference
  Category category;

  @Column(nullable = false, unique = true)
  String name;

  @Column(nullable = false)
  BigDecimal price;

  @Column(nullable = false, columnDefinition = "TEXT")
  String description;

  @Convert(converter = ProductOptionConverter.class)
  @Column(columnDefinition = "JSON")
  ProductOption options;

  @Enumerated(EnumType.STRING)
  @Column(length = 30)
  SellStatus sellStatus;

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
  @BatchSize(size = 50)
  @OrderBy("sortOrder asc")
  List<ProductImage> productImageList = new ArrayList<>();

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  LocalDateTime createdAt;

  @Column
  @UpdateTimestamp
  LocalDateTime updatedAt;

  @Column
  LocalDateTime deletedAt;

  @Builder
  public Product(Category category, String name, BigDecimal price, String description,
      ProductOption options, SellStatus sellStatus) {
    this.category = category;
    this.name = name;
    this.price = price;
    this.description = description;
    this.options = options;
    this.sellStatus = sellStatus;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setOptions(ProductOption options) {
    this.options = options;
  }

  public void setSellStatus(SellStatus sellStatus) {
    this.sellStatus = sellStatus;
  }

  public void setDeletedAt(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

  public boolean hasColorAndSize(String color, String size) {
    return this.options.getColors()
        .stream().filter(c -> c.getColorName().equals(color))
        .flatMap(c -> c.getSizes().stream())
        .anyMatch(s -> s.getSizeName().equals(size));
  }

  public void decreaseProductStock(String color, String size, int quantity) {
    SizeOption sizeOption = this.options.getColors()
        .stream().filter(c -> c.getColorName().equals(color))
        .flatMap(c -> c.getSizes().stream())
        .filter(s -> s.getSizeName().equals(size))
        .findFirst()
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT_OPTIONS));

    if (quantity > sizeOption.getStock()) {
      throw new ServiceException(ServiceExceptionCode.INSUFFICIENT_STOCK);
    }

    sizeOption.setStock(sizeOption.getStock() - quantity);
  }
}
