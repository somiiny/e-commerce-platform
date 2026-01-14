package com.sparta.camp.java.FinalProject.domain.purchase.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.enums.Role;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSummaryResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import com.sparta.camp.java.FinalProject.global.config.QueryDslConfig;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;


@DataJpaTest
@Import({
    QueryDslConfig.class,
    PurchaseQueryRepository.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PurchaseQueryRepositoryTest {

  @Autowired
  PurchaseQueryRepository purchaseQueryRepository;

  @Autowired
  TestEntityManager em;

  User userA;
  User userB;

  @BeforeEach
  void setUp() {

    userA = User.builder()
        .email("userA@test.com")
        .name("userA")
        .role(Role.ROLE_USER)
        .password("password1")
        .phoneNumber("010-123-4567")
        .build();
    em.persist(userA);

    userB = User.builder()
        .email("userB@test.com")
        .name("userB")
        .role(Role.ROLE_USER)
        .password("password2")
        .phoneNumber("010-123-4568")
        .build();
    em.persist(userB);

    for (int i = 0; i < 25; i++) {
      Purchase p = Purchase.builder()
          .user(userA)
          .purchaseNo("A-" + i)
          .totalPrice(BigDecimal.valueOf(50000))
          .purchaseStatus(
              i % 2 == 0 ? PurchaseStatus.PURCHASE_CREATED : PurchaseStatus.PURCHASE_COMPLETED
          )
          .receiverName("userA")
          .zipCode("12345")
          .shippingAddress("test")
          .phoneNumber("010-123-4568")
          .build();
      em.persist(p);
    }

    for (int i = 0; i < 5; i++) {
      Purchase p = Purchase.builder()
          .user(userB)
          .purchaseNo("B-" + i)
          .totalPrice(BigDecimal.valueOf(60000))
          .purchaseStatus(PurchaseStatus.PURCHASE_CREATED)
          .receiverName("userB")
          .zipCode("12346")
          .shippingAddress("test")
          .phoneNumber("010-123-4568")
          .build();
      em.persist(p);
    }

    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("사용자별로 주문 목록이 조회된다")
  void findAllByUserId_should_return_only_user_purchases() {

    PaginationRequest request = new PaginationRequest();
    ReflectionTestUtils.setField(request, "page", 0);
    ReflectionTestUtils.setField(request, "size", 10);

    List<PurchaseSummaryResponse> result =
        purchaseQueryRepository.findAllByUserId(userA.getId(), request);

    assertThat(result).hasSize(10);
    assertThat(result)
        .allMatch(r -> r.getPurchaseNo().startsWith("A-"));
  }

  @Test
  @DisplayName("페이지와 사이즈에 따라 주문 목록이 페이징된다")
  void paging_should_apply_offset_and_limit_correctly() {

    PaginationRequest page1 = new PaginationRequest();
    ReflectionTestUtils.setField(page1, "page", 1);
    ReflectionTestUtils.setField(page1, "size", 5);

    PaginationRequest page2 = new PaginationRequest();
    ReflectionTestUtils.setField(page2, "page", 2);
    ReflectionTestUtils.setField(page2, "size", 5);

    List<PurchaseSummaryResponse> firstPage =
        purchaseQueryRepository.findAllByUserId(userA.getId(), page1);

    List<PurchaseSummaryResponse> secondPage =
        purchaseQueryRepository.findAllByUserId(userA.getId(), page2);

    assertThat(firstPage).hasSize(5);
    assertThat(secondPage).hasSize(5);
    assertThat(firstPage.get(0).getId())
        .isNotEqualTo(secondPage.get(0).getId());
  }

  @Test
  @DisplayName("주문은 생성일 기준 내림차순으로 정렬된다")
  void sort_should_order_by_createdAt_desc() {

    PaginationRequest request = new PaginationRequest();
    ReflectionTestUtils.setField(request, "page", 1);
    ReflectionTestUtils.setField(request, "size", 10);

    List<PurchaseSummaryResponse> result =
        purchaseQueryRepository.findAllByUserId(userA.getId(), request);

    assertThat(result)
        .isSortedAccordingTo(
            Comparator.comparing(PurchaseSummaryResponse::getCreatedAt).reversed()
        );
  }

  @Test
  @DisplayName("사용자의 주문이 없는 경우 빈 리스트를 반환한다")
  void findAllByUserId_should_return_empty_list_when_no_purchases() {

    PaginationRequest request = new PaginationRequest();
    ReflectionTestUtils.setField(request, "page", 1);
    ReflectionTestUtils.setField(request, "size", 10);

    Long notExistUserId = 999L;

    List<PurchaseSummaryResponse> result =
        purchaseQueryRepository.findAllByUserId(notExistUserId, request);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("페이지 범위를 초과하면 빈 리스트를 반환한다")
  void findAllByUserId_should_return_empty_list_when_page_out_of_range() {

    PaginationRequest request = new PaginationRequest();
    ReflectionTestUtils.setField(request, "page", 4);
    ReflectionTestUtils.setField(request, "size", 10);

    List<PurchaseSummaryResponse> result =
        purchaseQueryRepository.findAllByUserId(userA.getId(), request);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

}