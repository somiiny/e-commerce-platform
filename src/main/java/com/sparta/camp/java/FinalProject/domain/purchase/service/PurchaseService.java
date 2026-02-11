package com.sparta.camp.java.FinalProject.domain.purchase.service;

import com.sparta.camp.java.FinalProject.common.enums.CreatorType;
import com.sparta.camp.java.FinalProject.common.enums.HistoryType;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseProductStatus;
import com.sparta.camp.java.FinalProject.common.enums.PurchaseStatus;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.common.pagination.PaginationRequest;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseService {

  private final UserRepository userRepository;

  private final CartRepository cartRepository;
  private final CartProductRepository cartProductRepository;

  private final ProductRepository productRepository;
  private final ProductOptionRepository productOptionRepository;

  private final PurchaseMapper purchaseMapper;
  private final PurchaseRepository purchaseRepository;
  private final PurchaseQueryRepository purchaseQueryRepository;

  private final HistoryRepository historyRepository;

  record PurchaseItem(
      Product product,
      ProductOption option,
      int quantity,
      BigDecimal unitPrice
  ) {}

  record ShippingInfo(
      String receiverName,
      String phoneNumber,
      String zipCode,
      String address,
      String detailAddress
  ) {
    public static ShippingInfo from(PurchaseCreateRequest request) {
      return new ShippingInfo(
          request.getReceiverName(),
          request.getPhoneNumber(),
          request.getZipCode(),
          request.getShippingAddress(),
          request.getShippingDetailAddress()
      );
    }

    public static ShippingInfo from(DirectPurchaseCreateRequest req) {
      return new ShippingInfo(
          req.getReceiverName(),
          req.getPhoneNumber(),
          req.getZipCode(),
          req.getShippingAddress(),
          req.getShippingDetailAddress()
      );
    }
  }

  record HistoryItem(Purchase purchase,
                     PurchaseStatus oldStatus,
                     PurchaseStatus newStatus,
                     String description,
                     CreatorType creatorType,
                     Long createdBy) {}

  @Transactional(readOnly = true)
  public List<PurchaseSummaryResponse> getPurchases(String userName, PaginationRequest request) {
    User user = getUserByEmail(userName);
    return purchaseQueryRepository.findAllByUserId(user.getId(), request);
  }

  @Transactional(readOnly = true)
  public PurchaseResponse getPurchase(String userName, Long purchaseId) {
    User user = getUserByEmail(userName);
    Purchase purchase = getPurchaseById(user.getId(), purchaseId);
    return purchaseMapper.toResponse(purchase);
  }

  @Transactional
  public PurchaseResponse createPurchaseDirect(String userName, DirectPurchaseCreateRequest request) {

    User user = this.getUserByEmail(userName);

    Product product = productRepository.findProductById(request.getProductId())
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT));

    ProductOption option = productOptionRepository.findByIdAndProductId(product.getId(),
            request.getProductOptionId())
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT_OPTIONS));

    if (option.getStock() < request.getQuantity()) {
      throw new ServiceException(ServiceExceptionCode.INSUFFICIENT_STOCK);
    }

    PurchaseItem purchaseItem = new PurchaseItem(product, option, request.getQuantity(), product.getPrice());
    Purchase newPurchase = createPurchase(user, List.of(purchaseItem), ShippingInfo.from(request));

    createHistory(new HistoryItem(newPurchase, null, PurchaseStatus.PURCHASE_CREATED,
        "주문생성", CreatorType.USER, user.getId()));

    return purchaseMapper.toResponse(newPurchase);
  }

  @Transactional
  public PurchaseResponse createPurchaseFromCart(String userName, PurchaseCreateRequest request) {

    User user = this.getUserByEmail(userName);

    Cart cart = this.getCartByUserId(user.getId());
    List<CartProduct> cartProducts = cartProductRepository.findAllByIn(cart.getId(),
        request.getCartProductIds());
    if (cartProducts.isEmpty() || cartProducts.size() != request.getCartProductIds().size()) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_CART_PRODUCT);
    }

    Set<Long> cartProductOptionIds = cartProducts.stream()
        .map(cp -> cp.getOption().getId())
        .collect(Collectors.toSet());

    List<ProductOption> validOptions = productOptionRepository.findAllValidByIds(new ArrayList<>(cartProductOptionIds));
    if (validOptions.size() != cartProductOptionIds.size()) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_PRODUCT_OPTIONS);
    }

    validateStock(validOptions, cartProducts);

    List<PurchaseItem> purchaseItems = createPurchaseItemFromCart(cartProducts);
    Purchase newPurchase = createPurchase(user, purchaseItems, ShippingInfo.from(request));

    createHistory(new HistoryItem(newPurchase, null, PurchaseStatus.PURCHASE_CREATED,
        "주문생성", CreatorType.USER, user.getId()));

    return purchaseMapper.toResponse(newPurchase);
  }

  @Transactional
  public void cancelPurchase(String userName, Long purchaseId) {

    User user = getUserByEmail(userName);
    Purchase purchase = getPurchaseById(user.getId(), purchaseId);

    if (!purchase.isCancelable()) {
      throw new ServiceException(ServiceExceptionCode.INVALID_PURCHASE_STATUS);
    }

    PurchaseStatus oldStatus = purchase.getPurchaseStatus();
    purchase.setPurchaseStatus(PurchaseStatus.PURCHASE_CANCELED);
    for (PurchaseProduct purchaseProduct : purchase.getPurchaseProductList()) {
      purchaseProduct.setStatus(PurchaseProductStatus.CANCELED);
    }

    createHistory(new HistoryItem(purchase, oldStatus, PurchaseStatus.PURCHASE_CANCELED,
        "주문취소", CreatorType.USER, user.getId()));
  }

  private User getUserByEmail(String email) {
    return userRepository.findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_USER));
  }

  private Purchase getPurchaseById(Long userId, Long purchaseId) {
    return purchaseRepository.findByUserIdAndPurchaseId(userId, purchaseId)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PURCHASE));
  }

  private Cart getCartByUserId(Long id) {
    return cartRepository.findByUserId(id)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_CART));
  }

  private void validateStock(List<ProductOption> validOptions, List<CartProduct> cartProducts) {
    Map<Long, Integer> requiredQtyByOptionId =
        cartProducts.stream()
            .collect(Collectors.toMap(
                cp -> cp.getOption().getId(),
                CartProduct::getQuantity,
                Integer::sum
            ));

    for (ProductOption productOption : validOptions) {
      if (productOption.getStock() < requiredQtyByOptionId.get(productOption.getId())) {
        throw new ServiceException(ServiceExceptionCode.INSUFFICIENT_STOCK);
      }
    }
  }

  private List<PurchaseItem> createPurchaseItemFromCart(List<CartProduct> cartProducts) {
    return cartProducts.stream()
        .map(cp ->
          new PurchaseItem(
              cp.getProduct(),
              cp.getOption(),
              cp.getQuantity(),
              cp.getProduct().getPrice()
          ))
        .toList();
  }

  private Purchase createPurchase(User user,
      List<PurchaseItem> purchaseItems,
      ShippingInfo shippingInfo) {

    BigDecimal totalAmount = purchaseItems.stream()
        .map(pi ->
          pi.unitPrice
              .multiply(BigDecimal.valueOf(pi.quantity))
        )
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    Purchase newPurchase = Purchase.builder()
        .user(user)
        .purchaseNo(PurchaseNoGenerator.generate())
        .totalPrice(totalAmount)
        .purchaseStatus(PurchaseStatus.PURCHASE_CREATED)
        .receiverName(shippingInfo.receiverName)
        .phoneNumber(shippingInfo.phoneNumber)
        .zipCode(shippingInfo.zipCode)
        .shippingAddress(shippingInfo.address)
        .shippingDetailAddress(shippingInfo.detailAddress)
        .build();

    for (PurchaseItem purchaseItem : purchaseItems) {
      PurchaseProduct purchaseProduct = PurchaseProduct.builder()
          .product(purchaseItem.product)
          .purchasedOption(purchaseItem.option)
          .quantity(purchaseItem.quantity)
          .priceAtPurchase(purchaseItem.unitPrice)
          .status(PurchaseProductStatus.PAID)
          .build();
      newPurchase.addPurchaseProduct(purchaseProduct);
    }

    purchaseRepository.save(newPurchase);
    return newPurchase;
  }

  private void createHistory(HistoryItem historyItem) {
    History history = History.builder()
        .historyType(HistoryType.PURCHASE)
        .purchase(historyItem.purchase())
        .oldStatus(String.valueOf(historyItem.oldStatus()))
        .newStatus(String.valueOf(historyItem.newStatus()))
        .description(historyItem.description())
        .creatorType(historyItem.creatorType())
        .createdBy(historyItem.createdBy())
        .build();
    historyRepository.save(history);
  }

}
