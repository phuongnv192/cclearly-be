package com.swp391.cclearly.service;

import com.swp391.cclearly.dto.base.ApiResponse;
import com.swp391.cclearly.dto.order.CreateOrderRequest;
import com.swp391.cclearly.dto.order.OrderPageResponse;
import com.swp391.cclearly.dto.order.OrderResponse;
import com.swp391.cclearly.dto.order.ReturnRequest;
import com.swp391.cclearly.dto.prescription.SavePrescriptionRequest;
import com.swp391.cclearly.entity.Address;
import com.swp391.cclearly.entity.Cart;
import com.swp391.cclearly.entity.Order;
import com.swp391.cclearly.entity.OrderItem;
import com.swp391.cclearly.entity.Payment;
import com.swp391.cclearly.entity.Prescription;
import com.swp391.cclearly.entity.ProductImage;
import com.swp391.cclearly.entity.ProductVariant;
import com.swp391.cclearly.entity.Refund;
import com.swp391.cclearly.entity.User;
import com.swp391.cclearly.exception.BadRequestException;
import com.swp391.cclearly.exception.ResourceNotFoundException;
import com.swp391.cclearly.repository.AddressRepository;
import com.swp391.cclearly.repository.CartRepository;
import com.swp391.cclearly.repository.OrderItemRepository;
import com.swp391.cclearly.repository.OrderRepository;
import com.swp391.cclearly.repository.RefundRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final AddressRepository addressRepository;
  private final RefundRepository refundRepository;
  private final OrderItemRepository orderItemRepository;

  public ApiResponse<List<OrderResponse>> getUserOrders(User user) {
    List<Order> orders = orderRepository.findByUserOrderByOrderIdDesc(user);
    List<OrderResponse> response = orders.stream().map(this::toResponse).collect(Collectors.toList());
    return ApiResponse.success("Lấy danh sách đơn hàng thành công", response);
  }

  public ApiResponse<OrderResponse> getOrderById(User user, UUID orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

    if (!order.getUser().getUserId().equals(user.getUserId())
        && !user.getRole().getRoleName().equals("ADMIN")
        && !user.getRole().getRoleName().equals("SALES_STAFF")) {
      throw new BadRequestException("Không có quyền xem đơn hàng này");
    }

    return ApiResponse.success("Lấy thông tin đơn hàng thành công", toResponse(order));
  }

  public ApiResponse<OrderResponse> createOrder(User user, CreateOrderRequest request) {
    Cart cart = cartRepository.findByUser(user)
        .orElseThrow(() -> new BadRequestException("Giỏ hàng trống"));

    if (cart.getCartItems().isEmpty()) {
      throw new BadRequestException("Giỏ hàng trống");
    }

    // Resolve or create address
    Address address;
    if (request.getAddressId() != null) {
      address = addressRepository.findById(request.getAddressId())
          .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));
    } else {
      address = Address.builder()
          .user(user)
          .street(request.getStreet())
          .city(request.getCity())
          .isDefault(false)
          .build();
      address = addressRepository.save(address);
    }

    // Build order
    String orderCode = "CC" + System.currentTimeMillis();

    // Check if any cart item is a preorder variant
    boolean hasPreorder = cart.getCartItems().stream()
        .anyMatch(ci -> Boolean.TRUE.equals(ci.getVariant().getIsPreorder()));

    boolean isDeposit = hasPreorder && "DEPOSIT".equalsIgnoreCase(request.getPaymentType());

    Order order = Order.builder()
        .user(user)
        .code(orderCode)
        .status("PENDING")
        .address(address)
        .isPreorder(hasPreorder ? true : null)
        .preorderDeadline(hasPreorder ? java.time.LocalDate.now().plusDays(7) : null)
        .paymentType(hasPreorder ? (isDeposit ? "DEPOSIT" : "FULL") : null)
        .build();

    // Build order items from cart
    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;

    for (var cartItem : cart.getCartItems()) {
      ProductVariant v = cartItem.getVariant();
      BigDecimal price = v.getSalePrice() != null ? v.getSalePrice() : v.getProduct().getBasePrice();

      // If deposit mode and the variant is preorder, charge 50%
      if (isDeposit && Boolean.TRUE.equals(v.getIsPreorder())) {
        price = price.multiply(new BigDecimal("0.5"));
      }

      BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
      total = total.add(lineTotal);

      OrderItem oi = OrderItem.builder()
          .order(order)
          .variant(v)
          .lensVariant(cartItem.getLensVariant())
          .unitPrice(price)
          .build();
      orderItems.add(oi);
    }

    order.setFinalAmount(total);
    order.getOrderItems().addAll(orderItems);
    order = orderRepository.save(order);

    // Clear cart
    cart.getCartItems().clear();
    cartRepository.save(cart);

    return ApiResponse.success("Đặt hàng thành công", toResponse(order));
  }

  public ApiResponse<Void> cancelOrder(User user, UUID orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

    if (!order.getUser().getUserId().equals(user.getUserId())) {
      throw new BadRequestException("Không có quyền hủy đơn hàng này");
    }

    if (!order.getStatus().equals("PENDING") && !order.getStatus().equals("CONFIRMED")) {
      throw new BadRequestException("Không thể hủy đơn hàng ở trạng thái hiện tại");
    }

    order.setStatus("CANCELLED");
    orderRepository.save(order);

    return ApiResponse.success("Hủy đơn hàng thành công", null);
  }

  public ApiResponse<Void> updateOrderStatus(UUID orderId, String status) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
    order.setStatus(status.toUpperCase());
    orderRepository.save(order);
    return ApiResponse.success("Cập nhật trạng thái đơn hàng thành công", null);
  }

  /**
   * Cập nhật trạng thái đơn hàng kèm ghi chú (tracking number, notes...)
   */
  public ApiResponse<Void> updateOrderStatusWithNote(UUID orderId, String status, String note) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
    order.setStatus(status.toUpperCase());
    // If note looks like a tracking number (for shipped status), save it
    if (note != null && !note.isBlank()) {
      if ("SHIPPED".equalsIgnoreCase(status)) {
        order.setTrackingNumber(note);
      }
    }
    orderRepository.save(order);
    return ApiResponse.success("Cập nhật trạng thái đơn hàng thành công", null);
  }

  /**
   * Lưu thông tin đơn kính (prescription) cho order item
   * FE gửi: rightEye(sph/cyl/axis/add), leftEye(sph/cyl/axis/add), pd, imageUrl
   */
  public ApiResponse<Void> savePrescription(UUID orderId, SavePrescriptionRequest request) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

    // Find the first order item (or by orderItemId if provided)
    OrderItem targetItem = null;
    if (request.getOrderItemId() != null && !request.getOrderItemId().isBlank()) {
      UUID itemId = UUID.fromString(request.getOrderItemId());
      targetItem = order.getOrderItems().stream()
          .filter(oi -> oi.getOrderItemId().equals(itemId))
          .findFirst()
          .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong đơn hàng"));
    } else {
      // Save prescription for the first item in the order
      targetItem = order.getOrderItems().stream().findFirst()
          .orElseThrow(() -> new BadRequestException("Đơn hàng không có sản phẩm"));
    }

    Prescription prescription = targetItem.getPrescription();
    if (prescription == null) {
      prescription = Prescription.builder()
          .orderItem(targetItem)
          .sphOd(request.getSphOd())
          .cylOd(request.getCylOd())
          .axisOd(request.getAxisOd())
          .addOd(request.getAddOd())
          .sphOs(request.getSphOs())
          .cylOs(request.getCylOs())
          .axisOs(request.getAxisOs())
          .addOs(request.getAddOs())
          .pd(request.getPd())
          .imageUrl(request.getImageUrl())
          .validationStatus("PENDING")
          .salesNote(request.getSalesNote())
          .build();
      targetItem.setPrescription(prescription);
    } else {
      if (request.getSphOd() != null) prescription.setSphOd(request.getSphOd());
      if (request.getCylOd() != null) prescription.setCylOd(request.getCylOd());
      if (request.getAxisOd() != null) prescription.setAxisOd(request.getAxisOd());
      if (request.getAddOd() != null) prescription.setAddOd(request.getAddOd());
      if (request.getSphOs() != null) prescription.setSphOs(request.getSphOs());
      if (request.getCylOs() != null) prescription.setCylOs(request.getCylOs());
      if (request.getAxisOs() != null) prescription.setAxisOs(request.getAxisOs());
      if (request.getAddOs() != null) prescription.setAddOs(request.getAddOs());
      if (request.getPd() != null) prescription.setPd(request.getPd());
      if (request.getImageUrl() != null) prescription.setImageUrl(request.getImageUrl());
      if (request.getSalesNote() != null) prescription.setSalesNote(request.getSalesNote());
    }

    orderItemRepository.save(targetItem);

    return ApiResponse.success("Lưu thông tin đơn kính thành công", null);
  }

  // Admin: get all orders with pagination & filters
  @Transactional(readOnly = true)
  public ApiResponse<OrderPageResponse> getAllOrders(String status, int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    Page<Order> orderPage;
    if (status != null && !status.isBlank()) {
      orderPage = orderRepository.findByStatusOrderByOrderIdDesc(status.toUpperCase(), pageable);
    } else {
      orderPage = orderRepository.findAllByOrderByOrderIdDesc(pageable);
    }

    List<OrderResponse> items = orderPage.getContent().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());

    OrderPageResponse response = OrderPageResponse.builder()
        .items(items)
        .meta(OrderPageResponse.Meta.builder()
            .page(page)
            .size(size)
            .totalElements(orderPage.getTotalElements())
            .totalPages(orderPage.getTotalPages())
            .build())
        .build();

    return ApiResponse.success("Lấy danh sách đơn hàng thành công", response);
  }

  // Customer: request return/refund
  public ApiResponse<Void> requestReturn(User user, UUID orderId, ReturnRequest request) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

    if (!order.getUser().getUserId().equals(user.getUserId())) {
      throw new BadRequestException("Không có quyền yêu cầu trả hàng cho đơn này");
    }

    if (!"DELIVERED".equals(order.getStatus())) {
      throw new BadRequestException("Chỉ có thể yêu cầu trả hàng cho đơn đã giao");
    }

    Refund refund = Refund.builder()
        .order(order)
        .amount(order.getFinalAmount())
        .reason(request.getReason())
        .status("PENDING")
        .createdAt(Instant.now())
        .build();
    refundRepository.save(refund);

    order.setStatus("RETURN_REQUESTED");
    orderRepository.save(order);

    return ApiResponse.success("Yêu cầu trả hàng đã được gửi", null);
  }

  private OrderResponse toResponse(Order o) {
    List<OrderResponse.OrderItemResponse> items = o.getOrderItems().stream()
        .map(oi -> {
          ProductVariant v = oi.getVariant();
          String imageUrl = v.getImages().stream()
              .map(ProductImage::getImageUrl)
              .findFirst()
              .orElse(null);
          // Determine product type
          String productType = v.getProduct().getCategoryType();
          return OrderResponse.OrderItemResponse.builder()
              .orderItemId(oi.getOrderItemId())
              .productName(v.getProduct().getName())
              .variantSku(v.getSku())
              .colorName(v.getColorName())
              .productType(productType)
              .unitPrice(oi.getUnitPrice())
              .quantity(1)
              .imageUrl(imageUrl)
              .build();
        })
        .collect(Collectors.toList());

    // Determine order type: prescription if any item has prescription
    String type = o.getOrderItems().stream()
        .anyMatch(oi -> oi.getPrescription() != null) ? "prescription" : "standard";

    // Get payment method from first payment if exists
    String paymentMethod = o.getPayments() != null && !o.getPayments().isEmpty()
        ? o.getPayments().iterator().next().getClass().getSimpleName()
        : null;

    return OrderResponse.builder()
        .orderId(o.getOrderId())
        .userId(o.getUser() != null ? o.getUser().getUserId() : null)
        .code(o.getCode())
        .status(o.getStatus())
        .type(type)
        .finalAmount(o.getFinalAmount())
        .customerEmail(o.getUser() != null ? o.getUser().getEmail() : null)
        .trackingNumber(o.getTrackingNumber())
        .shippingStreet(o.getAddress() != null ? o.getAddress().getStreet() : null)
        .shippingCity(o.getAddress() != null ? o.getAddress().getCity() : null)
        .shippingPhone(o.getUser() != null ? o.getUser().getPhoneNumber() : null)
        .recipientName(o.getUser() != null ? o.getUser().getFullName() : null)
        .isPreorder(o.getIsPreorder())
        .preorderDeadline(o.getPreorderDeadline())
        .paymentType(o.getPaymentType())
        .createdAt(o.getCreatedAt())
        .items(items)
        .build();
  }
}
