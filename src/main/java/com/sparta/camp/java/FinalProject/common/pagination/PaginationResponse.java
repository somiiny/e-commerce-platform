package com.sparta.camp.java.FinalProject.common.pagination;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaginationResponse<T> {

  int currentPage;

  int lastPage;

  int startPage;

  int endPage;

  long totalItems;

  boolean hasPrevious;

  boolean hasNext;

  List<T> content;

  @Builder
  public PaginationResponse(PaginationRequest paginationRequest, long totalItems, List<T> content) {
    this.currentPage = paginationRequest.getPage();

    this.lastPage = (int) Math.ceil((double) totalItems /paginationRequest.getSize());
    this.endPage = (int) Math.ceil((double) paginationRequest.getPage() / 10) * 10;
    this.endPage = Math.min(endPage, lastPage);
    this.startPage = Math.max((this.endPage - 9), 1);

    this.totalItems = totalItems;

    this.hasPrevious = this.startPage > 1;
    this.hasNext = this.endPage < totalItems;

    this.content = content;
  }
}
