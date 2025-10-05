package com.sparta.camp.java.FinalProject.domain.purchase.dto;

import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseStatusUpdateRequest {

  Long purchaseId;

  PurchaseStatus status;

  String reason;
}
