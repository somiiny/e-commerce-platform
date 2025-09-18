package com.sparta.camp.java.FinalProject.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminDeleteRequest {

  @NotBlank
  @Size(min = 8, max = 16, message = "비밀번호는 최소 8자리 이상 입력해주세요.")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=])[A-Za-z\\d!@#$%^&*()_+=]{8,16}$",
      message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다."
  )
  String currentPassword;

}
