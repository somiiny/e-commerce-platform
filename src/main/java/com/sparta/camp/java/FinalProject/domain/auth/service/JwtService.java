package com.sparta.camp.java.FinalProject.domain.auth.service;

import com.sparta.camp.java.FinalProject.domain.auth.dto.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.access-token-expiration}")
  private long accessTokenExpiration;

  @Value("${security.jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  private final RedisTemplate<String, String> redisTemplate;

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts
        .parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public String generateAccessToken(CustomUserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, accessTokenExpiration);
  }

  public String generateRefreshToken(CustomUserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, refreshTokenExpiration);
  }

  private String buildToken(Map<String, Object> extraClaims, CustomUserDetails userDetails, long expiration) {
    return Jwts
        .builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public boolean isTokenValid(String token, CustomUserDetails userDetails) {
    final String username = extractUsername(token);
    boolean isBlacklisted = redisTemplate.hasKey("blacklist:" + token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) && !isBlacklisted;
  }

  private boolean isTokenExpired(String token) {
    return this.extractExpiration(token).before(new Date());
  }

  public long getExpiration(String token) {
    Date expiration = this.extractExpiration(token);
    long now = System.currentTimeMillis();
    return (expiration.getTime() - now) / 1000;
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public void saveRefreshToken(String email, String refreshToken, long durationDays) {
    redisTemplate.opsForValue().set(
        "refreshToken:" + email,
        refreshToken,
        Duration.ofDays(durationDays)
    );
  }

  public void removeRefreshToken(String email) {
    redisTemplate.delete("refreshToken:" + email);
  }

  public void registerBlacklist(String token, long expiration) {
    redisTemplate.opsForValue().set(
        "blacklist:" + token,
        "logout",
        Duration.ofSeconds(expiration)
    );
  }

}
