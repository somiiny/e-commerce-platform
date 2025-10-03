package com.sparta.camp.java.FinalProject.domain.cart.service;

import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.cart.controller.CartController;
import com.sparta.camp.java.FinalProject.domain.cart.dto.CartProductCreateRequest;
import com.sparta.camp.java.FinalProject.domain.cart.dto.CartProductResponse;
import com.sparta.camp.java.FinalProject.domain.cart.dto.CartProductUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.cart.dto.CartResponse;
import com.sparta.camp.java.FinalProject.domain.cart.entity.Cart;
import com.sparta.camp.java.FinalProject.domain.cart.entity.CartProduct;
import com.sparta.camp.java.FinalProject.domain.cart.repository.CartProductQueryRepository;
import com.sparta.camp.java.FinalProject.domain.cart.repository.CartProductRepository;
import com.sparta.camp.java.FinalProject.domain.cart.repository.CartRepository;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import com.sparta.camp.java.FinalProject.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

  private final CartRepository cartRepository;
  private final UserRepository userRepository;
  private final CartProductRepository cartProductRepository;
  private final CartProductQueryRepository cartProductQueryRepository;
  private final ProductRepository productRepository;
  private final CartController cartController;

  public CartResponse getCartProduct(String userName) {

    User user = getUser(userName);

    Cart cart = getCart(user.getId());

    List<CartProduct> cartProductList = cartProductQueryRepository.findAll(cart.getId());
    List<CartProductResponse> cartProductResponseList = cartProductList.stream()
        .map(cartProduct -> CartProductResponse.builder()
    .id(cartProduct.getId())
    .cartId(cart.getId())
    .productId(cartProduct.getProduct().getId())
    .productName(cartProduct.getProduct().getName())
    .quantity(cartProduct.getQuantity())
    .price(cartProduct.getProduct().getPrice())
    .build()).toList();

    BigDecimal totalPrice = cartProductResponseList.stream()
        .map(cp -> cp.getPrice().multiply(BigDecimal.valueOf(cp.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return CartResponse.builder()
        .cartId(cart.getId())
        .cartProductList(cartProductResponseList)
        .totalPrice(totalPrice)
        .build();
  }

  public void createCartProduct(String userName, CartProductCreateRequest request) {

    User user = getUser(userName);

    Cart cart = getCart(user.getId());

    CartProduct cartProduct = getCartProduct(cart.getId(), request.getProductId());
    if (cartProduct == null) {
      Product product = productRepository.findProductById(request.getProductId())
          .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT));

      cartProduct = CartProduct.builder()
          .cart(cart)
          .product(product)
          .quantity(request.getQuantity())
          .build();

    } else {
      cartProduct.increaseQuantity(request.getQuantity());
    }

    cartProductRepository.save(cartProduct);
  }

  public void updateCartProduct (Long productId, String userName, CartProductUpdateRequest request) {

    User user = getUser(userName);

    Cart cart = getCart(user.getId());

    CartProduct cartProduct = getCartProduct(cart.getId(), productId);
    if (cartProduct == null) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_CART_PRODUCT);
    }

    cartProduct.setQuantity(request.getQuantity());
    cartProductRepository.save(cartProduct);
  }

  public void deleteCartProduct(Long productId, String userName) {
    User user = getUser(userName);

    Cart cart = getCart(user.getId());

    CartProduct cartProduct = getCartProduct(cart.getId(), productId);
    if (cartProduct == null) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_CART_PRODUCT);
    }

    cartProduct.setDeletedAt(LocalDateTime.now());
    cartProductRepository.save(cartProduct);
  }

  private User getUser (String userName) {
    return userRepository.findByEmailAndDeletedAtIsNull(userName)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_USER));
  }

  private Cart getCart (Long userId) {
    return cartRepository.findByUserId(userId)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_CART));
  }

  private CartProduct getCartProduct (Long cartId, Long productId) {
    return cartProductRepository.findByCartAndProductId(cartId, productId);
  }

}
