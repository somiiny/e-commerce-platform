package com.sparta.camp.java.FinalProject.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUpdateRequest {

  @NotBlank
  String name;

  @NotBlank
  @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "유효하지 않은 전화번호 입니다.")
  String phoneNumber;

  @Size(max = 10)
  String zipCode;

  String address;

  String detailAddress;

  LocalDate birthDate;

}
