package com.sparta.camp.java.FinalProject.domain.purchase.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.enums.Role;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationResponse;
import com.sparta.camp.java.FinalProject.domain.cart.entity.Cart;
import com.sparta.camp.java.FinalProject.domain.cart.entity.CartProduct;
import com.sparta.camp.java.FinalProject.domain.cart.repository.CartProductRepository;
import com.sparta.camp.java.FinalProject.domain.cart.repository.CartRepository;
import com.sparta.camp.java.FinalProject.domain.history.entity.History;
import com.sparta.camp.java.FinalProject.domain.history.repository.HistoryRepository;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductOption;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductOptionRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.DirectPurchaseCreateRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseCreateRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseSummaryResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.PurchaseProduct;
import com.sparta.camp.java.FinalProject.domain.purchase.generator.PurchaseNoGenerator;
import com.sparta.camp.java.FinalProject.domain.purchase.mapper.PurchaseMapper;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseQueryRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseRepository;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import com.sparta.camp.java.FinalProject.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

  @InjectMocks
  private PurchaseService purchaseService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CartRepository cartRepository;

  @Mock
  private CartProductRepository cartProductRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductOptionRepository productOptionRepository;

  @Mock
  private PurchaseMapper purchaseMapper;

  @Mock
  private PurchaseRepository purchaseRepository;

  @Mock
  private PurchaseQueryRepository purchaseQueryRepository;

  @Mock
  private HistoryRepository historyRepository;

  private User user;
  private Cart cart;
  private Product product;
  private ProductOption productOption;
  private List<CartProduct> cartProductList = new ArrayList<>();
  private List<ProductOption> productOptionList = new ArrayList<>();
  private Purchase purchase;
  private PurchaseResponse response;
  private PaginationRequest paginationRequest;
  private DirectPurchaseCreateRequest directRequest;
  private PurchaseCreateRequest cartRequest;
  private List<PurchaseSummaryResponse> summaryList = new ArrayList<>();

  @BeforeEach
  void setUp() {

    paginationRequest = new PaginationRequest();
    ReflectionTestUtils.setField(paginationRequest, "page", 0);
    ReflectionTestUtils.setField(paginationRequest, "size", 10);

    user = User.builder()
        .email("test@email.com")
        .role(Role.ROLE_USER)
        .build();
    ReflectionTestUtils.setField(user, "id", 1L);

    product = createProduct(1L, "product1", 50000);
    productOption = createProductOption(1L, product, 20);

    Product product2 = createProduct(2L, "product2", 10000);
    ProductOption po2 = createProductOption(2L, product2, 20);

    Product product3 = createProduct(3L, "product3", 30000);
    ProductOption po3 = createProductOption(3L, product3, 30);

    productOptionList = List.of(
        productOption, po2, po3
    );

    cart = new Cart();
    ReflectionTestUtils.setField(cart, "id", 1L);
    ReflectionTestUtils.setField(cart, "user", user);

    cartProductList = List.of(
        createCartProduct(1L, product, productOption, 10),
        createCartProduct(2L, product2, po2, 20),
        createCartProduct(3L, product3, po3, 30)
    );

    purchase = Purchase.builder()
        .user(user)
        .purchaseNo(PurchaseNoGenerator.generate())
        .build();
    ReflectionTestUtils.setField(purchase, "id", 1L);

    response = PurchaseResponse.builder()
        .id(1L)
        .purchaseNo(purchase.getPurchaseNo())
        .build();

    for (int i = 0; i < 20; i++) {
      PurchaseSummaryResponse response =
          createSummaryResponse((long) (i+1), PurchaseNoGenerator.generate());
      summaryList.add(response);
    }

    directRequest = new DirectPurchaseCreateRequest();
    ReflectionTestUtils.setField(directRequest, "productId", 1L);
    ReflectionTestUtils.setField(directRequest, "productOptionId", 1L);
    ReflectionTestUtils.setField(directRequest, "quantity", 10);

    cartRequest = new PurchaseCreateRequest();
    List<Long> cartProductIds = List.of(1L, 2L, 3L);
    ReflectionTestUtils.setField(cartRequest, "cartProductIds", cartProductIds);

  }

  private Product createProduct(Long id, String name, int price) {
    Product product = Product.builder()
        .name(name)
        .price(BigDecimal.valueOf(price))
        .build();
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  private ProductOption createProductOption(Long id, Product p, int stock) {
    ProductOption po = ProductOption.builder()
        .product(p)
        .stock(stock)
        .build();
    ReflectionTestUtils.setField(po, "id", id);
    return po;
  }

  private CartProduct createCartProduct(Long id,
      Product p,
      ProductOption option,
      Integer quantity) {

    CartProduct cp = CartProduct.builder()
        .cart(cart)
        .product(p)
        .option(option)
        .quantity(quantity)
        .build();
    ReflectionTestUtils.setField(cp, "id", id);
    return cp;
  }

  private PurchaseSummaryResponse createSummaryResponse(
      Long id,
      String purchaseNo
  ) {
    return PurchaseSummaryResponse.builder()
        .id(id)
        .purchaseNo(purchaseNo)
        .build();
  }

  @Test
  @DisplayName("사용자의 주문 목록을 페이징하여 조회한다")
  void getPurchases_should_return_all_purchases() {

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(purchaseQueryRepository.findAllByUserId(user.getId(), paginationRequest))
        .thenReturn(summaryList);
    when(purchaseQueryRepository.countPurchasesByUserId(user.getId()))
        .thenReturn((long) summaryList.size());

    PaginationResponse<PurchaseSummaryResponse> results = purchaseService.getPurchases(user.getEmail(), paginationRequest);

    assertThat(results.getContent().size()).isEqualTo(summaryList.size());
    assertThat(results.getTotalItems()).isEqualTo(summaryList.size());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(purchaseQueryRepository)
        .findAllByUserId(user.getId(), paginationRequest);
    verify(purchaseQueryRepository).countPurchasesByUserId(user.getId());
  }


  @Test
  @DisplayName("존재하지 않는 사용자인 경우 오류가 발생한다.")
  void getPurchases_should_throwException_when_user_is_not_exist() {

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> purchaseService.getPurchases(user.getEmail(), paginationRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_USER.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verifyNoMoreInteractions(userRepository, purchaseQueryRepository);
  }

  @Test
  @DisplayName("주문내역 상세 조회한다.")
  void getPurchase_should_return_purchase_detail() {

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(purchaseRepository.findByUserIdAndPurchaseId(user.getId(), purchase.getId()))
        .thenReturn(Optional.of(purchase));
    when(purchaseMapper.toResponse(purchase)).thenReturn(response);

    PurchaseResponse results = purchaseService.getPurchase(user.getEmail(), purchase.getId());

    assertThat(results).isNotNull();
    assertThat(results.getPurchaseNo()).isEqualTo(purchase.getPurchaseNo());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(purchaseRepository).findByUserIdAndPurchaseId(user.getId(), purchase.getId());
    verify(purchaseMapper).toResponse(purchase);
  }

  @Test
  @DisplayName("존재하지 않는 주문인 경우 오류가 발생한다.")
  void getPurchase_should_throwException_when_purchase_is_not_exist() {

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(purchaseRepository.findByUserIdAndPurchaseId(user.getId(), purchase.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> purchaseService.getPurchase(user.getEmail(), purchase.getId()))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PURCHASE.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(purchaseRepository).findByUserIdAndPurchaseId(user.getId(), purchase.getId());
    verifyNoMoreInteractions(userRepository, purchaseRepository);
  }

  @Test
  @DisplayName("바로구매로 주문한 상품을 정상적으로 등록한다.")
  void createPurchaseDirect_should_create_direct_purchase() {
    ReflectionTestUtils.setField(productOption, "stock", 15);

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(productRepository.findProductById(product.getId()))
        .thenReturn(Optional.of(product));
    when(productOptionRepository.findByIdAndProductId(product.getId(), productOption.getId()))
        .thenReturn(Optional.of(productOption));
    when(purchaseRepository.save(any(Purchase.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    when(historyRepository.save(any(History.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    purchaseService.createPurchaseDirect(user.getEmail(), directRequest);

    ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
    verify(purchaseRepository).save(captor.capture());

    Purchase newPurchase = captor.getValue();
    assertThat(newPurchase.getPurchaseStatus()).isEqualTo(PurchaseStatus.PURCHASE_CREATED);
    assertThat(newPurchase.getTotalPrice()).isEqualTo(product.getPrice().multiply(
        BigDecimal.valueOf(directRequest.getQuantity())));

    assertThat(newPurchase.getPurchaseProductList()).hasSize(1);

    PurchaseProduct pp = newPurchase.getPurchaseProductList().get(0);
    assertThat(pp.getProduct().getId()).isEqualTo(product.getId());
    assertThat(pp.getPurchasedOption().getId()).isEqualTo(productOption.getId());
    assertThat(pp.getQuantity()).isEqualTo(directRequest.getQuantity());
    assertThat(pp.getPriceAtPurchase()).isEqualTo(product.getPrice());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(productRepository).findProductById(product.getId());
    verify(productOptionRepository).findByIdAndProductId(product.getId(), productOption.getId());
    verify(historyRepository).save(any(History.class));
  }

  @Test
  @DisplayName("존재하지 않는 상품인 경우 오류가 발생한다.")
  void createPurchaseDirect_should_throwException_when_product_is_not_exist() {

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(productRepository.findProductById(directRequest.getProductId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> purchaseService.createPurchaseDirect(user.getEmail(), directRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PRODUCT.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(productRepository).findProductById(directRequest.getProductId());
    verifyNoMoreInteractions(userRepository, productRepository, productOptionRepository,
        purchaseRepository, historyRepository);
  }

  @Test
  @DisplayName("존재하지 않는 옵션인 경우 오류가 발생한다.")
  void createPurchaseDirect_should_throwException_when_product_option_is_not_exist() {

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
      .thenReturn(Optional.of(user));
    when(productRepository.findProductById(directRequest.getProductId()))
      .thenReturn(Optional.of(product));
    when(productOptionRepository.findByIdAndProductId(product.getId(), directRequest.getProductOptionId()))
      .thenReturn(Optional.empty());

    assertThatThrownBy(() -> purchaseService.createPurchaseDirect(user.getEmail(), directRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PRODUCT_OPTIONS.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(productRepository).findProductById(directRequest.getProductId());
    verify(productOptionRepository).findByIdAndProductId(product.getId(), directRequest.getProductOptionId());
    verifyNoMoreInteractions(userRepository, productRepository, productOptionRepository,
        purchaseRepository, historyRepository);
  }

  @Test
  @DisplayName("재고보다 많은 수량을 요청한 경우 오류가 발생한다.")
  void createPurchaseDirect_should_throwException_when_stock_is_lacking() {
    ReflectionTestUtils.setField(directRequest, "quantity", 30);

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(productRepository.findProductById(directRequest.getProductId()))
        .thenReturn(Optional.of(product));
    when(productOptionRepository.findByIdAndProductId(product.getId(), directRequest.getProductOptionId()))
        .thenReturn(Optional.of(productOption));

    assertThatThrownBy(() -> purchaseService.createPurchaseDirect(user.getEmail(), directRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.INSUFFICIENT_STOCK.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(productRepository).findProductById(directRequest.getProductId());
    verify(productOptionRepository).findByIdAndProductId(product.getId(), directRequest.getProductOptionId());
    verifyNoMoreInteractions(userRepository, productRepository, productOptionRepository,
        purchaseRepository, historyRepository);

  }

  @Test
  @DisplayName("재고와 동일한 수량을 요청한 경우 정상적으로 주문이 생성된다.")
  void createPurchaseDirect_should_create_purchase_when_stock_and_request_quantity_is_same() {
    ReflectionTestUtils.setField(directRequest, "quantity", 20);

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(productRepository.findProductById(product.getId()))
        .thenReturn(Optional.of(product));
    when(productOptionRepository.findByIdAndProductId(product.getId(), productOption.getId()))
        .thenReturn(Optional.of(productOption));
    when(purchaseRepository.save(any(Purchase.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(historyRepository.save(any(History.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    purchaseService.createPurchaseDirect(user.getEmail(), directRequest);

    ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
    verify(purchaseRepository).save(captor.capture());

    Purchase newPurchase = captor.getValue();
    assertThat(newPurchase.getPurchaseStatus()).isEqualTo(PurchaseStatus.PURCHASE_CREATED);
    assertThat(newPurchase.getTotalPrice()).isEqualTo(product.getPrice().multiply(
        BigDecimal.valueOf(directRequest.getQuantity())));

    assertThat(newPurchase.getPurchaseProductList()).hasSize(1);

    PurchaseProduct pp = newPurchase.getPurchaseProductList().get(0);
    assertThat(pp.getProduct().getId()).isEqualTo(product.getId());
    assertThat(pp.getPurchasedOption().getId()).isEqualTo(productOption.getId());
    assertThat(pp.getQuantity()).isEqualTo(directRequest.getQuantity());
    assertThat(pp.getPriceAtPurchase()).isEqualTo(product.getPrice());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(productRepository).findProductById(product.getId());
    verify(productOptionRepository).findByIdAndProductId(product.getId(), productOption.getId());
    verify(historyRepository).save(any(History.class));
  }

  @Test
  @DisplayName("장바구니로 주문한 상품들을 정상적으로 등록한다.")
  void createPurchaseFromCart_should_create_purchase_successfully() {

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(cartRepository.findByUserId(user.getId()))
        .thenReturn(Optional.of(cart));
    when(cartProductRepository.findAllByIn(cart.getId(), cartRequest.getCartProductIds()))
        .thenReturn(cartProductList);
    when(productOptionRepository.findAllValidByIds(anyList())).thenReturn(productOptionList);
    when(purchaseRepository.save(any(Purchase.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(historyRepository.save(any(History.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PurchaseResponse result = purchaseService.createPurchaseFromCart(user.getEmail(), cartRequest);

    ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
    verify(purchaseRepository).save(captor.capture());

    Purchase purchase = captor.getValue();
    assertThat(purchase.getPurchaseStatus()).isEqualTo(PurchaseStatus.PURCHASE_CREATED);
    assertThat(purchase.getPurchaseProductList()).hasSize(3);

    PurchaseProduct pp = purchase.getPurchaseProductList().stream()
        .filter(p -> p.getPurchasedOption().getId()
            .equals(productOptionList.get(1).getId()))
        .findFirst()
        .orElseThrow();

    assertThat(pp.getProduct().getId())
        .isEqualTo(cartProductList.get(1).getProduct().getId());
    assertThat(pp.getQuantity())
        .isEqualTo(cartProductList.get(1).getQuantity());
    assertThat(pp.getPriceAtPurchase())
        .isEqualTo(cartProductList.get(1).getProduct().getPrice());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());

    verify(cartRepository).findByUserId(user.getId());
    verify(cartProductRepository).findAllByIn(cart.getId(), cartRequest.getCartProductIds());
    verify(productOptionRepository).findAllValidByIds(anyList());
    verify(purchaseRepository).save(any(Purchase.class));
    verify(historyRepository).save(any(History.class));

  }

  @Test
  @DisplayName("장바구니 정보가 존재하지 않은 경우 오류가 발생한다.")
  void createPurchaseFromCart_should_throwException_when_cart_is_not_exist() {

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(cartRepository.findByUserId(user.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> purchaseService.createPurchaseFromCart(user.getEmail(), cartRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_CART.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(cartRepository).findByUserId(user.getId());
    verifyNoMoreInteractions(userRepository, cartRepository, cartProductRepository,
        productOptionRepository, purchaseRepository, historyRepository);
  }

  @Test
  @DisplayName("장바구니 상품이 존재하지 않은 경우 오류가 발생한다.")
  void createPurchaseFromCart_should_throwException_when_cartProducts_are_not_found() {

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(cartRepository.findByUserId(user.getId()))
        .thenReturn(Optional.of(cart));
    when(cartProductRepository.findAllByIn(cart.getId(), cartRequest.getCartProductIds()))
        .thenReturn(List.of());

    assertThatThrownBy(() -> purchaseService.createPurchaseFromCart(user.getEmail(), cartRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_CART_PRODUCT.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(cartRepository).findByUserId(user.getId());
    verify(cartProductRepository).findAllByIn(cart.getId(), cartRequest.getCartProductIds());
    verifyNoMoreInteractions(userRepository, cartRepository, cartProductRepository,
        productOptionRepository, purchaseRepository, historyRepository);
  }

  @Test
  @DisplayName("장바구니 상품과 데이터베이스 존재하는 상품 수가 일치하지 않는 경우 오류가 발생한다.")
  void createPurchaseFromCart_should_throwException_when_some_cartProducts_are_not_found() {
    cartProductList = List.of(
        createCartProduct(1L, product, productOption, 10)
    );

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(cartRepository.findByUserId(user.getId()))
        .thenReturn(Optional.of(cart));
    when(cartProductRepository.findAllByIn(cart.getId(), cartRequest.getCartProductIds()))
        .thenReturn(cartProductList);

    assertThatThrownBy(() -> purchaseService.createPurchaseFromCart(user.getEmail(), cartRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_CART_PRODUCT.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(cartRepository).findByUserId(user.getId());
    verify(cartProductRepository).findAllByIn(cart.getId(), cartRequest.getCartProductIds());
    verifyNoMoreInteractions(userRepository, cartRepository, cartProductRepository,
        productOptionRepository, purchaseRepository, historyRepository);
  }

  @Test
  @DisplayName("존재하지 않는 상품 옵션이 존재하는 경우 오류가 발생한다.")
  void createPurchaseFromCart_should_throwException_when_productOptions_are_not_found() {

    productOptionList = List.of(
        productOption
    );

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(cartRepository.findByUserId(user.getId()))
        .thenReturn(Optional.of(cart));
    when(cartProductRepository.findAllByIn(cart.getId(), cartRequest.getCartProductIds()))
        .thenReturn(cartProductList);
    when(productOptionRepository.findAllValidByIds(anyList()))
        .thenReturn(productOptionList);

    assertThatThrownBy(() -> purchaseService.createPurchaseFromCart(user.getEmail(), cartRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PRODUCT_OPTIONS.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(cartRepository).findByUserId(user.getId());
    verify(cartProductRepository).findAllByIn(cart.getId(), cartRequest.getCartProductIds());
    verify(productOptionRepository).findAllValidByIds(anyList());
    verifyNoMoreInteractions(userRepository, cartRepository, cartProductRepository,
        productOptionRepository, purchaseRepository, historyRepository);
  }

  @Test
  @DisplayName("재고보다 많은 수량을 요청한 경우 오류가 발생한다.")
  void createPurchaseFromCart_should_throwException_when_stock_is_lacking() {
    ReflectionTestUtils.setField(productOption, "stock", 1);

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(cartRepository.findByUserId(user.getId()))
        .thenReturn(Optional.of(cart));
    when(cartProductRepository.findAllByIn(cart.getId(), cartRequest.getCartProductIds()))
        .thenReturn(cartProductList);
    when(productOptionRepository.findAllValidByIds(anyList())).thenReturn(productOptionList);

    assertThatThrownBy(() -> purchaseService.createPurchaseFromCart(user.getEmail(), cartRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.INSUFFICIENT_STOCK.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());
    verify(cartRepository).findByUserId(user.getId());
    verify(cartProductRepository).findAllByIn(cart.getId(), cartRequest.getCartProductIds());
    verify(productOptionRepository).findAllValidByIds(anyList());
    verifyNoMoreInteractions(userRepository, cartRepository, cartProductRepository,
        productOptionRepository, purchaseRepository, historyRepository);
  }

  @Test
  @DisplayName("재고와 동일한 수량을 요청해도 정상적으로 주문이 생성된다.")
  void createPurchaseFromCart_should_create_purchase_when_stock_and_request_quantity_is_same() {
    ReflectionTestUtils.setField(productOption, "stock", 10);

    when(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
        .thenReturn(Optional.of(user));
    when(cartRepository.findByUserId(user.getId()))
        .thenReturn(Optional.of(cart));
    when(cartProductRepository.findAllByIn(cart.getId(), cartRequest.getCartProductIds()))
        .thenReturn(cartProductList);
    when(productOptionRepository.findAllValidByIds(anyList())).thenReturn(productOptionList);
    when(purchaseRepository.save(any(Purchase.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(historyRepository.save(any(History.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PurchaseResponse result = purchaseService.createPurchaseFromCart(user.getEmail(), cartRequest);

    ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
    verify(purchaseRepository).save(captor.capture());

    Purchase purchase = captor.getValue();
    assertThat(purchase.getPurchaseStatus()).isEqualTo(PurchaseStatus.PURCHASE_CREATED);
    assertThat(purchase.getPurchaseProductList()).hasSize(3);

    PurchaseProduct pp = purchase.getPurchaseProductList().stream()
        .filter(p -> p.getPurchasedOption().getId()
            .equals(productOptionList.get(1).getId()))
        .findFirst()
        .orElseThrow();

    assertThat(pp.getProduct().getId())
        .isEqualTo(cartProductList.get(1).getProduct().getId());
    assertThat(pp.getQuantity())
        .isEqualTo(cartProductList.get(1).getQuantity());
    assertThat(pp.getPriceAtPurchase())
        .isEqualTo(cartProductList.get(1).getProduct().getPrice());

    verify(userRepository).findByEmailAndDeletedAtIsNull(user.getEmail());

    verify(cartRepository).findByUserId(user.getId());
    verify(cartProductRepository).findAllByIn(cart.getId(), cartRequest.getCartProductIds());
    verify(productOptionRepository).findAllValidByIds(anyList());
    verify(purchaseRepository).save(any(Purchase.class));
    verify(historyRepository).save(any(History.class));
  }
}