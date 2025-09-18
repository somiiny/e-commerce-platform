package com.sparta.camp.java.FinalProject.domain.auth.controller;

import com.sparta.camp.java.FinalProject.common.response.ApiResponse;
import com.sparta.camp.java.FinalProject.domain.auth.dto.LoginRequest;
import com.sparta.camp.java.FinalProject.domain.auth.dto.LoginResponse;
import com.sparta.camp.java.FinalProject.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    return ApiResponse.success(authService.authenticate(loginRequest));
  }

  @GetMapping("/status")
  public ApiResponse<LoginResponse> checkStatus(HttpServletRequest request) {
    LoginResponse loginResponse = authService.checkLoginStatus(request.getHeader("Authorization"));
    return ApiResponse.success(loginResponse);
  }

  @GetMapping("/logout")
  public ApiResponse<Void> logout(HttpServletRequest request) {
    authService.logout(request.getHeader("Authorization"));
    return ApiResponse.success();
  }
}
