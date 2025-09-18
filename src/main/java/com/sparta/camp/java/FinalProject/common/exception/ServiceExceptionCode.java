package com.sparta.camp.java.FinalProject.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ServiceExceptionCode {
  NOT_FOUND_TOKEN("토큰을 찾을 수 없습니다."),
  NOT_VALID_TOKEN("유효한 토큰이 아닙니다."),

  DUPLICATE_EMAIL("이미 사용 중인 아이디입니다."),
  NOT_FOUND_USER("존재하지 않는 사용자입니다."),
  NOT_FOUND_ADMIN("존재하지 않는 관리자입니다."),

  DUPLICATE_CURRENT_PASSWORD("기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다."),
  NOT_MATCH_PASSWORD("비밀번호가 일치하지 않습니다.");


  final String message;
}
