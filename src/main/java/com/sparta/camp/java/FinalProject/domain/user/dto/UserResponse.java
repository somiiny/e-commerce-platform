package com.sparta.camp.java.FinalProject.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.camp.java.FinalProject.common.enums.Role;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

  Long id;

  String email;

  String name;

  Role role;

  String phoneNumber;

  String zipCode;

  String address;

  String detailAddress;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  LocalDate birthDate;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  LocalDateTime createdAt;
}
