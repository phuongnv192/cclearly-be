package com.swp391.cclearly.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateOrderRequest {
  @NotBlank(message = "Tên người nhận không được để trống")
  private String recipientName;

  @NotBlank(message = "Số điện thoại không được để trống")
  private String phone;

  @NotBlank(message = "Địa chỉ không được để trống")
  private String street;

  @NotBlank(message = "Thành phố không được để trống")
  private String city;

  private String notes;

  // Optional: reuse saved address
  private UUID addressId;

  private String paymentMethod; // cod, payos

  private String couponCode;
}
