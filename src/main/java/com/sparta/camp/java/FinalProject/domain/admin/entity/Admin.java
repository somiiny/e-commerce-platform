package com.sparta.camp.java.FinalProject.domain.admin.entity;

import com.sparta.camp.java.FinalProject.common.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
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
public class Admin {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false, unique = true, updatable = false)
  String email;

  @Column(nullable = false, length = 30)
  String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  Role role;

  @Column(nullable = false)
  String password;

  @Column(nullable = false, length = 30)
  String phoneNumber;

  @Column(length = 10)
  String zipCode;

  @Column
  String address;

  @Column
  String detailAddress;

  @Column
  LocalDate birthDate;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  LocalDateTime createdAt;

  @Column
  @UpdateTimestamp
  LocalDateTime updatedAt;

  @Column
  LocalDateTime deletedAt;

  @Builder
  public Admin(String email, Role role, String name, String password, String phoneNumber,
      String zipCode, String address, String detailAddress, LocalDate birthDate,
      LocalDateTime createdAt) {
    this.email = email;
    this.role = role;
    this.name = name;
    this.password = password;
    this.phoneNumber = phoneNumber;
    this.zipCode = zipCode;
    this.address = address;
    this.detailAddress = detailAddress;
    this.birthDate = birthDate;
    this.createdAt = createdAt;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setDetailAddress(String detailAddress) {
    this.detailAddress = detailAddress;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public void setDeletedAt(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

}
