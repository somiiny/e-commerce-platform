package com.sparta.camp.java.FinalProject.domain.purchase.service;

import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.enums.SellStatus;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
import com.sparta.camp.java.FinalProject.domain.cart.entity.Cart;
import com.sparta.camp.java.FinalProject.domain.cart.entity.CartProduct;
import com.sparta.camp.java.FinalProject.domain.cart.repository.CartProductRepository;
import com.sparta.camp.java.FinalProject.domain.cart.repository.CartRepository;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseCreateRequest;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseProductResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.dto.PurchaseResponse;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.Purchase;
import com.sparta.camp.java.FinalProject.domain.purchase.entity.PurchaseProduct;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseQueryRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.repository.PurchaseRepository;
import com.sparta.camp.java.FinalProject.domain.purchase.vo.PurchaseProductOption;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import com.sparta.camp.java.FinalProject.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseService {

  private final UserRepository userRepository;
  private final ProductRepository productRepository;

  private final CartRepository cartRepository;
  private final CartProductRepository cartProductRepository;

  private final PurchaseRepository purchaseRepository;
  private final PurchaseQueryRepository purchaseQueryRepository;

  public List<PurchaseResponse> getPurchases(String userName, PaginationRequest request) {

    User user = getUserByEmail(userName);

    List<Purchase> purchaseList = purchaseQueryRepository.findAllByUserId(user.getId(), request);
    if (purchaseList.isEmpty()) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_PURCHASE);
    }

    List<PurchaseResponse> responseList = new ArrayList<>();
    for (Purchase purchase : purchaseList) {
      PurchaseResponse response = convertToResponse(purchase);
      responseList.add(response);
    }

    return responseList;
  }

  public PurchaseResponse getPurchase(String userName, Long purchaseId) {

    User user = getUserByEmail(userName);

    Purchase purchase = purchaseRepository.findById(purchaseId)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PURCHASE));
    if (!purchase.getUser().getId().equals(user.getId())) {
      throw new ServiceException(ServiceExceptionCode.NOT_PERMIT_ACCESS);
    }

    purchase.getPurchaseProductList().size();
    purchase.getPurchaseProductList().forEach(pp -> pp.getProduct().getProductImageList().size());

    return convertToResponse(purchase);
  }

  public PurchaseResponse createPurchase(String userName, PurchaseCreateRequest request) {

    User user = getUserByEmail(userName);

    Cart cart = cartRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_CART));

    List<Product> products = productRepository.findAllByIn(request.getCartProductIds());
    if (products.size() != request.getCartProductIds().size()) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT);
    }
    Map<Long, Product> productMap = products.stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    List<CartProduct> cartProducts = cartProductRepository.findAllByCartId(cart.getId());
    if (cartProducts.size() != request.getCartProductIds().size()) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_CART_PRODUCT);
    }
    Map<Long, CartProduct> cartProductMap = cartProducts.stream()
        .collect(Collectors.toMap(cp -> cp.getProduct().getId(), cp -> cp));

    List<PurchaseProduct> purchaseProducts = new ArrayList<>();
    for (Long productId : request.getCartProductIds()) {

      Product product = productMap.get(productId);
      if (!product.getSellStatus().equals(SellStatus.ON_SALE)) {
        throw new ServiceException(ServiceExceptionCode.NOT_SALE_PRODUCT);
      }

      CartProduct cartProduct = cartProductMap.get(productId);
      if (!product.hasColorAndSize(cartProduct.getOptions().getColor(), cartProduct.getOptions().getSize())) {
        throw new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT_OPTIONS);
      }

      PurchaseProduct purchaseProduct = PurchaseProduct.builder()
          .product(product)
          .options(convertToPurchaseProductOption(cartProduct))
          .quantity(cartProduct.getQuantity())
          .priceAtPurchase(product.getPrice())
          .build();

      purchaseProducts.add(purchaseProduct);

      product.decreaseProductStock(purchaseProduct.getOptions().getColor(), purchaseProduct.getOptions()
          .getSize(), purchaseProduct.getQuantity());

      cartProduct.setDeletedAt(LocalDateTime.now());
    }

    BigDecimal totalPrice = purchaseProducts.stream()
        .map(pp -> pp.getPriceAtPurchase().multiply(BigDecimal.valueOf(pp.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    Purchase purchase = Purchase.builder()
        .user(user)
        .totalPrice(totalPrice)
        .purchaseStatus(PurchaseStatus.ORDER_PLACED)
        .receiverName(request.getReceiverName())
        .zipCode(request.getZipCode())
        .shippingAddress(request.getShippingAddress())
        .shippingDetailAddress(request.getShippingDetailAddress())
        .phoneNumber(request.getPhoneNumber())
        .build();

    purchaseProducts.forEach(purchase::addPurchaseProduct);
    purchaseRepository.save(purchase);

    return convertToResponse(purchase);
  }

  private User getUserByEmail(String email) {
    return userRepository.findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_USER));
  }

  private PurchaseResponse convertToResponse(Purchase purchase) {
    return PurchaseResponse.builder()
        .id(purchase.getId())
        .totalPrice(purchase.getTotalPrice())
        .status(purchase.getPurchaseStatus())
        .productList(purchase.getPurchaseProductList().stream()
            .map(pp -> PurchaseProductResponse.builder()
                .id(pp.getId())
                .purchaseId(pp.getPurchase().getId())
                .productId(pp.getProduct().getId())
                .productName(pp.getProduct().getName())
                .color(pp.getOptions().getColor())
                .size(pp.getOptions().getSize())
                .quantity(pp.getQuantity())
                .priceAtPurchase(pp.getPriceAtPurchase())
                .build()).toList())
        .receiverName(purchase.getReceiverName())
        .zipCode(purchase.getZipCode())
        .shippingAddress(purchase.getShippingAddress())
        .shippingDetailAddress(purchase.getShippingDetailAddress())
        .phoneNumber(purchase.getPhoneNumber())
        .build();
  }

  private PurchaseProductOption convertToPurchaseProductOption(CartProduct cartProduct) {
    return PurchaseProductOption.builder()
        .color(cartProduct.getOptions().getColor())
        .size(cartProduct.getOptions().getSize())
        .build();
  }
}
