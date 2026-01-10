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

  NOT_FOUND_CATEGORY("존재하지 않는 카테고리 입니다."),
  NOT_ALLOWED_SELF_PARENT("자기 자신을 부모 카테고리로 지정할 수 없습니다."),
  NOT_DELETE_CATEGORY("카테고리를 삭제할 수 없습니다."),

  DUPLICATE_EMAIL("이미 사용 중인 아이디입니다."),
  NOT_FOUND_USER("존재하지 않는 사용자입니다."),
  NOT_FOUND_ADMIN("존재하지 않는 관리자입니다."),

  DUPLICATE_CURRENT_PASSWORD("기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다."),
  NOT_MATCH_PASSWORD("비밀번호가 일치하지 않습니다."),

  NOT_FOUND_PRODUCT("상품이 존재하지 않습니다."),
  NOT_FOUND_PRODUCT_IMAGE("상품 이미지가 존재하지 않습니다."),
  DUPLICATE_PRODUCT_NAME("동일한 상품명이 존재합니다."),
  DUPLICATE_PRODUCT_OPTION("동일한 옵션이 존재합니다."),
  NOT_SALE_PRODUCT("현재 판매 중인 상품이 아닙니다."),

  NOT_FOUND_CART("장바구니가 존재하지 않습니다."),
  NOT_FOUND_CART_PRODUCT("장바구니에 해당 상품이 존재하지 않습니다."),

  INSUFFICIENT_STOCK("재고가 부족합니다."),
  NOT_FOUND_PURCHASE("주문 내역이 없습니다."),
  NOT_PERMIT_ACCESS("접근 권한이 없습니다."),

  DUPLICATE_STATUS("주문 상태가 동일합니다."),
  INVALID_STATUS_TRANSITION("유효하지 않은 주문 상태 변경입니다."),
  CANNOT_DELETE_PRODUCT("상품을 삭제할 수 없습니다."),
  NOT_FOUND_PRODUCT_OPTIONS("해당 상품 옵션이 존재하지 않습니다."),

  NOT_MATCH_PAYMENT_INFO("결제 정보가 유효하지 않습니다."),
  PAYMENT_FAILED("결제를 실패 하였습니다."),
  NOT_FOUND_PAYMENT("결제 내역이 없습니다."),
  INVALID_PURCHASE_STATUS("유효하지 않은 취소/환불 요청입니다."),
  EXCEEDS_PAYMENT_AMOUNT("환불 금액이 기존 결제 금액을 초과합니다."),
  ALREADY_REFUND_REQUEST("기존 환불 요청 건이 존재합니다."),

  NOT_FOUND_FILE("파일을 확인할 수 없습니다.")
  ;


  final String message;
}
