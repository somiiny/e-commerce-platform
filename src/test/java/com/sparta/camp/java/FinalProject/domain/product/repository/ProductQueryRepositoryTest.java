package com.sparta.camp.java.FinalProject.domain.product.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.camp.java.FinalProject.common.enums.SellStatus;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.domain.category.entity.Category;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductResponse;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductSearchRequest;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.global.config.QueryDslConfig;
import java.math.BigDecimal;
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
    ProductQueryRepository.class,
    QueryDslConfig.class
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductQueryRepositoryTest {

  @Autowired
  ProductQueryRepository productQueryRepository;

  @Autowired
  TestEntityManager em;

  private Category outer;
  private Category top;

  private PaginationRequest paginationRequest;

  @BeforeEach
  void setUp() {

    outer = createCategory("outer", null);
    em.persist(outer);
    top = createCategory("top", null);
    em.persist(top);

    persistProducts(outer, 1, 15, BigDecimal.valueOf(200000));
    persistProducts(top, 1, 5, BigDecimal.valueOf(35000));

    em.flush();
    em.clear();

    paginationRequest = new PaginationRequest();
    ReflectionTestUtils.setField(paginationRequest, "page", 1);
    ReflectionTestUtils.setField(paginationRequest, "size", 10);
  }

  private Category createCategory(
      String name,
      Category parent
  ) {
    return Category.builder()
        .name(name)
        .parent(parent)
        .build();
  }

  private void persistProducts(Category category,
      Integer start,
      Integer end,
      BigDecimal price) {

    IntStream.rangeClosed(start, end)
        .mapToObj(i -> Product.builder()
            .category(category)
            .price(price)
            .name("test_" + category.getName() + i)
            .description("test_" + category.getName() + i)
            .sellStatus(
                i % 2 == 0
                ? SellStatus.ON_SALE : SellStatus.OUT_OF_STOCK
            )
            .build())
        .forEach(em::persist);
  }


  @Test
  @DisplayName("검색 조건에 따른 상품 목록이 조회된다.")
  void findProducts_should_return_products_when_search_condition_exists() {

    ProductSearchRequest searchRequest = new ProductSearchRequest();
    ReflectionTestUtils.setField(searchRequest, "categoryId", outer.getId());
    ReflectionTestUtils.setField(searchRequest, "minPrice",200000);
    ReflectionTestUtils.setField(searchRequest, "sortType", "name");
    ReflectionTestUtils.setField(searchRequest, "sortDirection", "desc");
    ReflectionTestUtils.setField(searchRequest, "keywordType", "description");
    ReflectionTestUtils.setField(searchRequest, "keyword", "test_outer1");

    List<ProductResponse> results = productQueryRepository.findProducts(searchRequest, paginationRequest);

    long total = productQueryRepository.countProducts(searchRequest);

    assertThat(results).hasSize(7);
    assertThat(total).isEqualTo(7);

    assertThat(results)
        .extracting(ProductResponse::getDescription)
        .allMatch(desc -> desc.contains("test_outer1"));

    assertThat(results)
        .isSortedAccordingTo(Comparator.comparing(ProductResponse::getName).reversed());
  }

  @Test
  @DisplayName("검색 조건이 없는 경우 전체 상품 목록이 조회된다.")
  void findProducts_should_return_products_when_search_condition_does_not_exist() {
    ProductSearchRequest searchRequest = new ProductSearchRequest();
    ReflectionTestUtils.setField(searchRequest, "categoryId", top.getId());

    List<ProductResponse> results = productQueryRepository.findProducts(searchRequest, paginationRequest);

    long total = productQueryRepository.countProducts(searchRequest);

    assertThat(results).hasSize(5);
    assertThat(total).isEqualTo(5);

    assertThat(results)
        .extracting(ProductResponse::getName)
        .allMatch(name -> name.contains("test_top"));

    assertThat(results)
        .isSortedAccordingTo(Comparator.comparing(ProductResponse::getCreatedAt).reversed());
  }

  @Test
  @DisplayName("매칭되는 결과가 없는 경우 빈 리스트가 조회된다.")
  void findProducts_should_return_empty_list_when_condition_not_match() {
    ProductSearchRequest searchRequest = new ProductSearchRequest();
    ReflectionTestUtils.setField(searchRequest, "categoryId", top.getId());
    ReflectionTestUtils.setField(searchRequest,"minPrice",40000);

    List<ProductResponse> results = productQueryRepository.findProducts(searchRequest, paginationRequest);

    long total = productQueryRepository.countProducts(searchRequest);

    assertThat(results).isEmpty();
    assertThat(total).isEqualTo(0);

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

    ProductSearchRequest searchRequest = new ProductSearchRequest();
    ReflectionTestUtils.setField(searchRequest, "categoryId", outer.getId());

    List<ProductResponse> firstPage = productQueryRepository.findProducts(searchRequest, page1);
    List<ProductResponse> secondPage = productQueryRepository.findProducts(searchRequest, page2);

    long total = productQueryRepository.countProducts(searchRequest);

    assertThat(firstPage).hasSize(page1.getSize());
    assertThat(secondPage).hasSize(page2.getSize());
    assertThat(total).isEqualTo(15);

    assertThat(firstPage)
        .isSortedAccordingTo(
            Comparator.comparing(ProductResponse::getCreatedAt).reversed()
        );

    assertThat(firstPage)
        .extracting(ProductResponse::getId)
        .doesNotContainAnyElementsOf(
            secondPage.stream()
                .map(ProductResponse::getId)
                .toList()
        );
  }

  @Test
  @DisplayName("페이지 범위를 초과하면 빈 리스트를 반환한다")
  void findProducts_should_return_empty_list_when_page_out_of_range() {
    PaginationRequest lastPage = new PaginationRequest();
    ReflectionTestUtils.setField(lastPage, "page", 4);
    ReflectionTestUtils.setField(lastPage, "size", 5);

    ProductSearchRequest searchRequest = new ProductSearchRequest();
    ReflectionTestUtils.setField(searchRequest, "categoryId", outer.getId());

    List<ProductResponse> results = productQueryRepository.findProducts(searchRequest, lastPage);

    long total = productQueryRepository.countProducts(searchRequest);

    assertThat(results).isEmpty();
    assertThat(total).isEqualTo(15);

  }



}