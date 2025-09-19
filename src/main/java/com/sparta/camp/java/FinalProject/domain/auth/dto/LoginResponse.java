package com.sparta.camp.java.FinalProject.domain.auth.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {

  String email;

  String role;

  String accessToken;

  String refreshToken;

  public LoginResponse(String email, String role, String accessToken,String refreshToken) {
    this.email = email;
    this.role = role;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }
}
