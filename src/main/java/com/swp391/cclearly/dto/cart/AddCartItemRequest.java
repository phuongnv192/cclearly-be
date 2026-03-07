package com.swp391.cclearly.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class AddCartItemRequest {
  @NotNull(message = "Variant ID không được để trống")
  private UUID variantId;

  @NotNull(message = "Số lượng không được để trống")
  @Min(value = 1, message = "Số lượng phải ít nhất là 1")
  private Integer quantity;

  // Optional: for products that require a lens
  private UUID lensVariantId;
}
