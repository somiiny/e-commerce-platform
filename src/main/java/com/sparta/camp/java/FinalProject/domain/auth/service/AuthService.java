package com.sparta.camp.java.FinalProject.domain.auth.service;

import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.auth.dto.CustomUserDetails;
import com.sparta.camp.java.FinalProject.domain.auth.dto.LoginRequest;
import com.sparta.camp.java.FinalProject.domain.auth.dto.LoginResponse;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import com.sparta.camp.java.FinalProject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final RedisTemplate<String, String> redisTemplate;
  private final CustomUserDetailService userDetailsService;

  public LoginResponse authenticate(LoginRequest loginRequest) {

    User user = userRepository.findByEmailAndDeletedAtIsNull(loginRequest.getEmail())
            .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_USER));

    CustomUserDetails userDetails = new CustomUserDetails(user);

    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    jwtService.saveRefreshToken(user.getEmail(), refreshToken, 7);

    return new LoginResponse(user.getEmail(), user.getRole(), accessToken, refreshToken);
  }

  public LoginResponse checkLoginStatus(String authHeader) {
    String token = (authHeader != null && authHeader.startsWith("Bearer "))
        ? authHeader.substring(7) : null;

    if (token == null || token.isBlank()) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_TOKEN);
    }

    String email = jwtService.extractUsername(token);
    CustomUserDetails userDetails = userDetailsService.loadUserByUsername(email);

    if (!jwtService.isTokenValid(token, userDetails)) {
      throw new ServiceException(ServiceExceptionCode.NOT_VALID_TOKEN);
    }

    String refreshToken = redisTemplate.opsForValue().get("refreshToken:" + email);
    return new LoginResponse(token, refreshToken);
  }

  public void logout(String authHeader) {
    String accessToken = authHeader.substring(7);
    long expiration = jwtService.getExpiration(accessToken);
    String email = jwtService.extractUsername(accessToken);

    jwtService.registerBlacklist(accessToken, expiration);
    jwtService.removeRefreshToken(email);
  }

}
