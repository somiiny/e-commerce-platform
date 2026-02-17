package com.sparta.camp.java.FinalProject.domain.purchase.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.enums.Role;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSearchRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSummaryResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import com.sparta.camp.java.FinalProject.global.config.QueryDslConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;


@DataJpaTest
@Import({
    PurchaseQueryRepository.class,
    QueryDslConfig.class
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PurchaseQueryRepositoryTest {

  @Autowired
  PurchaseQueryRepository purchaseQueryRepository;

  @Autowired
  TestEntityManager em;

  User userA;
  User userB;

  private PaginationRequest paginationRequest;

  @BeforeEach
  void setUp() {
    userA = createUser(
        "userA@test.com",
        "userA",
        "passwordA1",
        "010-123-4567");
    em.persist(userA);

    userB = createUser(
        "userB@test.com",
        "userB",
        "passwordAB",
        "010-1234-5678"
    );
    em.persist(userB);

    persistPurchases(userA, 1, 25, BigDecimal.valueOf(50000));
    persistPurchases(userB, 1, 5, BigDecimal.valueOf(60000));

    System.out.println(
        em.getEntityManager().createQuery("select p.createdAt from Purchase p", LocalDateTime.class)
            .getResultList()
    );

    em.flush();
    em.clear();

    paginationRequest = new PaginationRequest();
    ReflectionTestUtils.setField(paginationRequest, "page", 1);
    ReflectionTestUtils.setField(paginationRequest, "size", 10);
  }

  private User createUser(
      String email,
      String name,
      String password,
      String phoneNumber
  ) {
    return User.builder()
        .email(email)
        .name(name)
        .role(Role.ROLE_USER)
        .password(password)
        .phoneNumber(phoneNumber)
        .build();
  }

  private void persistPurchases(User user,
      int start,
      int end,
      BigDecimal price) {

    IntStream.rangeClosed(start, end)
        .mapToObj(i -> Purchase.builder()
                  .user(user)
                  .purchaseNo("PUR-" + user.getName() + "-" + i)
                  .totalPrice(price)
                  .purchaseStatus(
                      i % 2 == 0
                          ? PurchaseStatus.PURCHASE_CREATED
                          : PurchaseStatus.PURCHASE_COMPLETED
                  )
                  .receiverName(user.getName())
                  .zipCode("12345")
                  .shippingAddress("test")
                  .phoneNumber(user.getPhoneNumber())
                  .build())
        .forEach(em::persist);
  }

  @Test
  @DisplayName("검색 조건에 따른 주문 목록이 조회된다.")
  void findAll_should_return_purchases_when_search_condition_exist() {

    PurchaseSearchRequest searchRequest = new PurchaseSearchRequest();
    ReflectionTestUtils.setField(searchRequest, "userEmail", "userB@test.com");
    ReflectionTestUtils.setField(searchRequest, "purchaseStatus", PurchaseStatus.PURCHASE_CREATED);
    ReflectionTestUtils.setField(searchRequest, "startDate", LocalDate.of(2026,2,17));
    ReflectionTestUtils.setField(searchRequest, "endDate", LocalDate.of(2026,2, 22));

    List<PurchaseSummaryResponse> results = purchaseQueryRepository.findAll(searchRequest, paginationRequest);

    long total = purchaseQueryRepository.countPurchases(searchRequest);

    assertThat(results).hasSize(2);
    assertThat(total).isEqualTo(2);

    assertThat(results)
        .extracting(PurchaseSummaryResponse::getPurchaseNo)
        .allMatch(no -> no.startsWith("PUR-userB"));

    assertThat(results)
        .extracting(PurchaseSummaryResponse::getUserEmail)
        .containsOnly("userB@test.com");

    assertThat(results)
        .extracting(PurchaseSummaryResponse::getStatus)
        .containsOnly(PurchaseStatus.PURCHASE_CREATED);

    assertThat(results)
        .isSortedAccordingTo(
            Comparator.comparing(PurchaseSummaryResponse::getCreatedAt).reversed()
        );
  }

  @Test
  @DisplayName("검색 조건이 없는 경우 전체 주문 목록이 조회된다.")
  void findAll_should_return_purchases_when_search_condition_not_exist() {
    PurchaseSearchRequest searchRequest = new PurchaseSearchRequest();

    List<PurchaseSummaryResponse> results = purchaseQueryRepository.findAll(searchRequest, paginationRequest);

    long total = purchaseQueryRepository.countPurchases(searchRequest);

    assertThat(results).hasSize(paginationRequest.getSize());

    assertThat(results)
        .isSortedAccordingTo(
            Comparator.comparing(PurchaseSummaryResponse::getCreatedAt).reversed()
        );

    assertThat(total).isEqualTo(30);
  }

  @Test
  @DisplayName("매칭되는 결과가 없는 경우 빈 리스트가 조회된다.")
  void findAll_should_return_empty_purchases_when_condition_not_match() {
    PurchaseSearchRequest searchRequest = new PurchaseSearchRequest();
    ReflectionTestUtils.setField(searchRequest, "userEmail", "userA@test.com");
    ReflectionTestUtils.setField(searchRequest, "purchaseStatus", PurchaseStatus.PURCHASE_PAID);

    List<PurchaseSummaryResponse> results = purchaseQueryRepository.findAll(searchRequest, paginationRequest);

    long total = purchaseQueryRepository.countPurchases(searchRequest);

    assertThat(results).isEmpty();
    assertThat(total).isEqualTo(0);
  }

  @Test
  @DisplayName("사용자별로 주문 목록이 조회된다")
  void findAllByUserId_should_return_only_user_purchases() {

    List<PurchaseSummaryResponse> result =
        purchaseQueryRepository.findAllByUserId(userA.getId(), paginationRequest);

    long total = purchaseQueryRepository.countPurchasesByUserId(userA.getId());

    assertThat(result).hasSize(paginationRequest.getSize());
    assertThat(total).isEqualTo(25);

    assertThat(result)
        .allMatch(r -> r.getPurchaseNo().startsWith("PUR-"+userA.getName()));

    assertThat(result)
        .isSortedAccordingTo(
            Comparator.comparing(PurchaseSummaryResponse::getCreatedAt).reversed()
                .thenComparing(PurchaseSummaryResponse::getStatus)
        );

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

    long total = purchaseQueryRepository.countPurchasesByUserId(userA.getId());

    assertThat(firstPage).hasSize(5);
    assertThat(secondPage).hasSize(5);
    assertThat(total).isEqualTo(25);

    assertThat(firstPage)
        .isSortedAccordingTo(
            Comparator.comparing(PurchaseSummaryResponse::getCreatedAt).reversed()
                .thenComparing(PurchaseSummaryResponse::getStatus)
        );

    assertThat(firstPage)
        .extracting(PurchaseSummaryResponse::getId)
        .doesNotContainAnyElementsOf(
            secondPage.stream()
                .map(PurchaseSummaryResponse::getId)
                .toList()
        );
  }

  @Test
  @DisplayName("페이지 범위를 초과하면 빈 리스트를 반환한다")
  void findAllByUserId_should_return_empty_list_when_page_out_of_range() {

    PaginationRequest request = new PaginationRequest();
    ReflectionTestUtils.setField(request, "page", 4);
    ReflectionTestUtils.setField(request, "size", 10);

    List<PurchaseSummaryResponse> result =
        purchaseQueryRepository.findAllByUserId(userA.getId(), request);

    long total = purchaseQueryRepository.countPurchasesByUserId(userA.getId());

    assertThat(result).isEmpty();
    assertThat(total).isEqualTo(25);
  }

}