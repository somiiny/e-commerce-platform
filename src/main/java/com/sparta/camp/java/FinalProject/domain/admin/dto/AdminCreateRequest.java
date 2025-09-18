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
public class AdminCreateRequest {

  @NotBlank
  @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
      message = "유효하지 않은 이메일 주소 입니다.")
  String email;

  @NotBlank
  String name;

  @NotBlank
  @Size(min = 8, max = 16, message = "비밀번호는 최소 8자리 이상 입력해주세요.")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=])[A-Za-z\\d!@#$%^&*()_+=]{8,16}$",
      message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다."
  )
  String password;

  @NotBlank
  @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "유효하지 않은 전화번호 입니다.")
  String phoneNumber;

  @Size(max = 10)
  String zipCode;

  String address;

  String detailAddress;

  LocalDate birthDate;
}
