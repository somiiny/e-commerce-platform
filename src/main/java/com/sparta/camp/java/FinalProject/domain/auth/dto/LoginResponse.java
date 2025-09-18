package com.sparta.camp.java.FinalProject.domain.auth.dto;

import com.sparta.camp.java.FinalProject.common.enums.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {

  String email;

  Role role;

  String accessToken;

  String refreshToken;

  public LoginResponse(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }

  public LoginResponse(String email, Role role, String accessToken,String refreshToken) {
    this.email = email;
    this.role = role;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }
}
