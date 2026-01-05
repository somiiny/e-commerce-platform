package com.sparta.camp.java.FinalProject.domain.cart.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.cart.dto.CartProductCreateRequest;
import com.sparta.camp.java.FinalProject.domain.cart.dto.CartProductResponse;
import com.sparta.camp.java.FinalProject.domain.cart.dto.CartProductUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.cart.dto.CartResponse;
import com.sparta.camp.java.FinalProject.domain.cart.entity.Cart;
import com.sparta.camp.java.FinalProject.domain.cart.entity.CartProduct;
import com.sparta.camp.java.FinalProject.domain.cart.mapper.CartProductMapper;
import com.sparta.camp.java.FinalProject.domain.cart.repository.CartProductRepository;
import com.sparta.camp.java.FinalProject.domain.cart.repository.CartRepository;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductOption;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductOptionRepository;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import com.sparta.camp.java.FinalProject.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
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
class CartServiceTest {

  @InjectMocks
  private CartService cartService;

  @Mock
  private CartRepository cartRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CartProductRepository cartProductRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private CartProductMapper cartProductMapper;

  @Mock
  private ProductOptionRepository productOptionRepository;

  private User test_user;
  private Cart test_cart;
  private Product product1;
  private Product product2;
  private ProductOption option1;
  private ProductOption option2;
  private List<CartProduct> cartProducts = new ArrayList<>();

  private CartProductCreateRequest createRequest;
  private CartProductUpdateRequest updateRequest;

  @BeforeEach
  void setUp() {
    test_user = User.builder()
        .email("test@email.com")
        .build();
    ReflectionTestUtils.setField(test_user, "id", 1L);

    test_cart = new Cart();
    ReflectionTestUtils.setField(test_cart, "id", 1L);
    ReflectionTestUtils.setField(test_cart, "user", test_user);

    product1 = createProduct(1L, "p1");
    product2 = createProduct(2L, "p2");
    option1 = createOption(1L, product1, 30);
    option2 = createOption(2L, product2, 40);

    cartProducts = List.of(
        createCartProduct(1L, test_cart, product1, option1, 10),
        createCartProduct(2L, test_cart, product2, option2, 5)
    );
    ReflectionTestUtils.setField(test_cart, "cartProducts", cartProducts);

    createRequest = new CartProductCreateRequest();
    ReflectionTestUtils.setField(createRequest, "productId", product1.getId());
    ReflectionTestUtils.setField(createRequest, "productOptionId",option1.getId());
    ReflectionTestUtils.setField(createRequest, "quantity", 5);

    updateRequest = new CartProductUpdateRequest();
    ReflectionTestUtils.setField(updateRequest, "id", cartProducts.get(1).getId());
    ReflectionTestUtils.setField(updateRequest, "productId", product2.getId());
    ReflectionTestUtils.setField(updateRequest, "productOptionId", option2.getId());
    ReflectionTestUtils.setField(updateRequest, "quantity", 3);

  }

  private CartProduct createCartProduct(
      Long id,
      Cart cart,
      Product product,
      ProductOption productOption,
      Integer quantity
  ) {
    CartProduct cartProduct = new CartProduct();
    ReflectionTestUtils.setField(cartProduct, "id", id);
    ReflectionTestUtils.setField(cartProduct, "cart", cart);
    ReflectionTestUtils.setField(cartProduct, "product", product);
    ReflectionTestUtils.setField(cartProduct, "option", productOption);
    cartProduct.setQuantity(quantity);
    return cartProduct;
  }

  private Product createProduct(
      Long id,
      String name
  ) {
    Product product = new Product();
    ReflectionTestUtils.setField(product, "id", id);
    ReflectionTestUtils.setField(product, "name", name);
    return product;
  }

  private ProductOption createOption(Long id, Product product, Integer stock) {
    ProductOption productOption = new ProductOption();
    ReflectionTestUtils.setField(productOption, "id", id);
    ReflectionTestUtils.setField(productOption, "product", product);
    ReflectionTestUtils.setField(productOption, "stock", stock);
    return productOption;
  }

  @Test
  @DisplayName("장바구니에 담긴 물건들을 조회한다.")
  void getCartProduct_should_return_cartProducts() {

    when(userRepository.findByEmailAndDeletedAtIsNull(test_user.getEmail()))
        .thenReturn(Optional.of(test_user));
    when(cartRepository.findByUserId(test_user.getId()))
        .thenReturn(Optional.of(test_cart));
    when(cartProductMapper.toResponse(any(CartProduct.class)))
        .thenAnswer(invocation -> {
          CartProduct cp = invocation.getArgument(0);
          return CartProductResponse.builder()
              .id(cp.getId())
              .productId(cp.getProduct().getId())
              .productOptionId(cp.getOption().getId())
              .quantity(cp.getQuantity())
              .build();
        });

    CartResponse result = cartService.getCartProduct(test_user.getEmail());

    assertThat(result).isNotNull();
    assertThat(result.getCartProductList()).hasSize(cartProducts.size());

    CartProductResponse cp1 = result.getCartProductList().get(0);
    assertThat(cp1.getProductId()).isEqualTo(cartProducts.get(0).getProduct().getId());
    assertThat(cp1.getQuantity()).isEqualTo(cartProducts.get(0).getQuantity());

    verify(userRepository).findByEmailAndDeletedAtIsNull(test_user.getEmail());
    verify(cartRepository).findByUserId(test_user.getId());
    verify(cartProductMapper, times(test_cart.getCartProducts().size()))
        .toResponse(any(CartProduct.class));

  }

  @Test
  @DisplayName("사용자가 존재하지 않는 경우 오류가 발생한다.")
  void getCartProduct_should_throwException_when_user_does_not_exist() {
    when(userRepository.findByEmailAndDeletedAtIsNull(test_user.getEmail()))
      .thenReturn(Optional.empty());

    assertThatThrownBy(() -> cartService.getCartProduct(test_user.getEmail()))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_USER.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(test_user.getEmail());
    verifyNoMoreInteractions(userRepository, cartRepository);
  }

  @Test
  @DisplayName("장바구니가 조회되지 않는 경우 오류가 발생한다.")
  void getCartProduct_should_throwException_when_cart_does_not_exist() {
    when(userRepository.findByEmailAndDeletedAtIsNull(test_user.getEmail()))
      .thenReturn(Optional.of(test_user));
    when(cartRepository.findByUserId(test_user.getId()))
      .thenReturn(Optional.empty());

    assertThatThrownBy(() -> cartService.getCartProduct(test_user.getEmail()))
    .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_CART.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(test_user.getEmail());
    verify(cartRepository).findByUserId(test_user.getId());
    verifyNoMoreInteractions(userRepository, cartRepository);
  }

  @Test
  @DisplayName("장바구니에 없는 상품인 경우 상품 추가가 정상적으로 진행된다.")
  void createCartProduct_should_create_cartProduct_successfully() {

    when(userRepository.findByEmailAndDeletedAtIsNull(test_user.getEmail()))
        .thenReturn(Optional.of(test_user));
    when(cartRepository.findByUserId(test_user.getId()))
        .thenReturn(Optional.of(test_cart));
    when(cartProductRepository.findExistingCartProduct(test_cart.getId(),
        createRequest.getProductId(),
        createRequest.getProductOptionId()))
        .thenReturn(null);
    when(productOptionRepository.findByProductOptionId(createRequest.getProductOptionId()))
        .thenReturn(Optional.of(option1));
    when(productRepository.findProductById(createRequest.getProductId()))
        .thenReturn(Optional.of(product1));
    when(cartProductRepository.save(any(CartProduct.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    cartService.createCartProduct(test_user.getEmail(), createRequest);

    ArgumentCaptor<CartProduct> captor = ArgumentCaptor.forClass(CartProduct.class);
    verify(cartProductRepository).save(captor.capture());

    CartProduct cp = captor.getValue();
    assertThat(cp.getCart()).isEqualTo(test_cart);
    assertThat(cp.getProduct().getId()).isEqualTo(createRequest.getProductId());
    assertThat(cp.getOption().getId()).isEqualTo(createRequest.getProductOptionId());
    assertThat(cp.getQuantity()).isEqualTo(createRequest.getQuantity());

    verify(userRepository).findByEmailAndDeletedAtIsNull(test_user.getEmail());
    verify(cartRepository).findByUserId(test_user.getId());
    verify(cartProductRepository).findExistingCartProduct(test_cart.getId(),
        createRequest.getProductId(),
        createRequest.getProductOptionId());
    verify(productOptionRepository).findByProductOptionId(createRequest.getProductOptionId());
    verify(productRepository).findProductById(createRequest.getProductId());
  }

  @Test
  @DisplayName("존재하지 않는 옵션인 경우 오류가 발생한다.")
  void createCartProduct_should_throwException_when_productOption_does_not_exist() {

    when(userRepository.findByEmailAndDeletedAtIsNull(test_user.getEmail()))
        .thenReturn(Optional.of(test_user));
    when(cartRepository.findByUserId(test_user.getId()))
        .thenReturn(Optional.of(test_cart));
    when(cartProductRepository.findExistingCartProduct(test_cart.getId(),
        createRequest.getProductId(),
        createRequest.getProductOptionId()))
        .thenReturn(null);
    when(productOptionRepository.findByProductOptionId(createRequest.getProductOptionId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> cartService.createCartProduct(test_user.getEmail(), createRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_PRODUCT_OPTIONS.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(test_user.getEmail());
    verify(cartRepository).findByUserId(test_user.getId());
    verify(cartProductRepository).findExistingCartProduct(test_cart.getId(),
        createRequest.getProductId(),
        createRequest.getProductOptionId());
    verify(productOptionRepository).findByProductOptionId(createRequest.getProductOptionId());

  }

  @Test
  @DisplayName("재고보다 많은 수량이 요청되는 경우 오류가 발생한다.")
  void createCartProduct_should_throwException_when_stock_is_not_sufficient() {
    ReflectionTestUtils.setField(createRequest, "quantity", 40);

    when(userRepository.findByEmailAndDeletedAtIsNull(test_user.getEmail()))
        .thenReturn(Optional.of(test_user));
    when(cartRepository.findByUserId(test_user.getId()))
        .thenReturn(Optional.of(test_cart));
    when(cartProductRepository.findExistingCartProduct(test_cart.getId(),
        createRequest.getProductId(),
        createRequest.getProductOptionId()))
        .thenReturn(null);
    when(productOptionRepository.findByProductOptionId(createRequest.getProductOptionId()))
        .thenReturn(Optional.of(option1));

    assertThatThrownBy(() -> cartService.createCartProduct(test_user.getEmail(), createRequest))
      .isInstanceOf(ServiceException.class)
      .hasMessageContaining(ServiceExceptionCode.INSUFFICIENT_STOCK.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(test_user.getEmail());
    verify(cartRepository).findByUserId(test_user.getId());
    verify(cartProductRepository).findExistingCartProduct(test_cart.getId(),
        createRequest.getProductId(),
        createRequest.getProductOptionId());
    verify(productOptionRepository).findByProductOptionId(createRequest.getProductOptionId());

  }

  @Test
  @DisplayName("이미 장바구니에 존재하는 상품인 경우 상품 수량을 추가한다.")
  void createCartProduct_should_increase_stock_when_cartProduct_already_exists() {

    int originalQuantity = cartProducts.get(0).getQuantity();

    when(userRepository.findByEmailAndDeletedAtIsNull(test_user.getEmail()))
        .thenReturn(Optional.of(test_user));
    when(cartRepository.findByUserId(test_user.getId()))
        .thenReturn(Optional.of(test_cart));
    when(cartProductRepository.findExistingCartProduct(test_cart.getId(),
        createRequest.getProductId(),
        createRequest.getProductOptionId()))
        .thenReturn(cartProducts.get(0));
    when(productOptionRepository.findByProductOptionId(createRequest.getProductOptionId()))
        .thenReturn(Optional.of(option1));

    cartService.createCartProduct(test_user.getEmail(), createRequest);

    assertThat(cartProducts.get(0).getQuantity())
        .isEqualTo(originalQuantity + createRequest.getQuantity());

    verify(userRepository).findByEmailAndDeletedAtIsNull(test_user.getEmail());
    verify(cartRepository).findByUserId(test_user.getId());
    verify(cartProductRepository).findExistingCartProduct(test_cart.getId(),
        createRequest.getProductId(),
        createRequest.getProductOptionId());
    verify(productOptionRepository).findByProductOptionId(createRequest.getProductOptionId());

  }

  @Test
  @DisplayName("상품 수량이 정상적으로 수정된다.")
  void updateCartProduct_should_set_cartProduct_quantity_successfully() {
    when(cartProductRepository.findByIdAndDeletedAtIsNull(cartProducts.get(1).getId()))
      .thenReturn(Optional.of(cartProducts.get(1)));

    cartService.updateCartProductQuantity(cartProducts.get(1).getId(), updateRequest);

    assertThat(cartProducts.get(1).getQuantity()).isEqualTo(updateRequest.getQuantity());
    verify(cartProductRepository).findByIdAndDeletedAtIsNull(cartProducts.get(1).getId());
  }

  @Test
  @DisplayName("장바구니에 상품이 존재하지 않는 경우 오류가 발생한다.")
  void updateCartProduct_should_throwException_when_cartProduct_does_not_exist() {
    when(cartProductRepository.findByIdAndDeletedAtIsNull(cartProducts.get(1).getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() ->
        cartService.updateCartProductQuantity(cartProducts.get(1).getId(), updateRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_CART_PRODUCT.getMessage());

    verify(cartProductRepository).findByIdAndDeletedAtIsNull(cartProducts.get(1).getId());
  }

  @Test
  @DisplayName("장바구니에서 해당 상품이 정상적으로 삭제된다.")
  void deleteCartProduct() {
    when(cartProductRepository.findByIdAndDeletedAtIsNull(cartProducts.get(1).getId()))
        .thenReturn(Optional.of(cartProducts.get(1)));

    cartService.deleteCartProduct(cartProducts.get(1).getId());

    assertThat(cartProducts.get(1).getDeletedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    verify(cartProductRepository).findByIdAndDeletedAtIsNull(cartProducts.get(1).getId());
  }
}