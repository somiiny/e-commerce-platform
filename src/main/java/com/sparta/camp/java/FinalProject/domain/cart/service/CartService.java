package com.sparta.camp.java.FinalProject.domain.cart.service;


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
  private final ProductRepository productRepository;

  private final CartProductMapper cartProductMapper;
  private final ProductOptionRepository productOptionRepository;

  public CartResponse getCartProduct(String userName) {

    User user = getUser(userName);
    Cart cart = getCart(user.getId());

    List<CartProductResponse> cartProductResponses = cart.getCartProducts().stream()
        .filter(cp -> cp.getDeletedAt() == null)
        .map(cartProductMapper::toResponse)
        .toList();

    return CartResponse.builder()
        .cartId(cart.getId())
        .cartProductList(cartProductResponses)
        .build();
  }

  public void createCartProduct(String userName, CartProductCreateRequest request) {

    User user = getUser(userName);
    Cart cart = getCart(user.getId());

    CartProduct cartProduct = cartProductRepository.findExistingCartProduct(cart.getId(),
        request.getProductId(), request.getProductOptionId());

    ProductOption selectedOption = productOptionRepository.findByProductOptionId(request.getProductOptionId())
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT_OPTIONS));

    if (cartProduct != null) {
      validateStock(selectedOption, cartProduct.getQuantity() + request.getQuantity());
      cartProduct.increaseQuantity(request.getQuantity());
      return;
    }

    validateStock(selectedOption, request.getQuantity());
    cartProduct = CartProduct.builder()
        .cart(cart)
        .product(getProduct(request.getProductId()))
        .option(selectedOption)
        .quantity(request.getQuantity())
        .build();

    cartProductRepository.save(cartProduct);
  }

  public void updateCartProductQuantity (Long cartProductId, CartProductUpdateRequest request) {
    CartProduct cartProduct = getCartProduct(cartProductId);
    validateStock(cartProduct.getOption(), request.getQuantity());
    cartProduct.setQuantity(request.getQuantity());
  }

  public void deleteCartProduct(Long cartProductId) {
    CartProduct cartProduct = getCartProduct(cartProductId);
    cartProduct.setDeletedAt(LocalDateTime.now());
  }

  private User getUser (String userName) {
    return userRepository.findByEmailAndDeletedAtIsNull(userName)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_USER));
  }

  private Cart getCart (Long userId) {
    return cartRepository.findByUserId(userId)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_CART));
  }

  private Product getProduct (Long productId) {
    return productRepository.findProductById(productId)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT));
  }

  private CartProduct getCartProduct (Long cartProductId) {
    return cartProductRepository.findByIdAndDeletedAtIsNull(cartProductId)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_CART_PRODUCT));
  }

  private void validateStock(ProductOption option, Integer quantity) {
    if (option.getStock() < quantity) {
      throw new ServiceException(ServiceExceptionCode.INSUFFICIENT_STOCK);
    }
  }

}
