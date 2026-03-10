package com.swp391.cclearly.controller;

import com.swp391.cclearly.dto.base.ApiResponse;
import com.swp391.cclearly.entity.Promotion;
import com.swp391.cclearly.repository.PromotionRepository;
import com.swp391.cclearly.repository.SystemConfigRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

  private final SystemConfigRepository systemConfigRepository;
  private final PromotionRepository promotionRepository;

  @GetMapping("/maintenance-status")
  public ApiResponse<Map<String, Object>> getMaintenanceStatus() {
    boolean maintenance = systemConfigRepository.findByConfigKey("maintenance_mode")
        .map(c -> "true".equalsIgnoreCase(c.getConfigValue()))
        .orElse(false);

    return ApiResponse.success("OK", Map.of("maintenance", maintenance));
  }

  @GetMapping("/shipping-config")
  public ApiResponse<Map<String, Object>> getShippingConfig() {
    BigDecimal defaultShippingFee = systemConfigRepository.findByConfigKey("default_shipping_fee")
        .map(c -> new BigDecimal(c.getConfigValue()))
        .orElse(new BigDecimal("30000"));

    BigDecimal freeShippingThreshold = systemConfigRepository.findByConfigKey("free_shipping_threshold")
        .map(c -> new BigDecimal(c.getConfigValue()))
        .orElse(new BigDecimal("500000"));

    Map<String, Object> config = Map.of(
        "defaultShippingFee", defaultShippingFee,
        "freeShippingThreshold", freeShippingThreshold
    );

    return ApiResponse.success("Lấy cấu hình vận chuyển thành công", config);
  }

  @PostMapping("/promotions/validate")
  public ApiResponse<Map<String, Object>> validateVoucher(@RequestBody Map<String, Object> body) {
    String code = ((String) body.getOrDefault("code", "")).toUpperCase().trim();
    BigDecimal orderTotal = body.get("orderTotal") != null
        ? new BigDecimal(body.get("orderTotal").toString())
        : BigDecimal.ZERO;

    if (code.isEmpty()) {
      return ApiResponse.error("Vui lòng nhập mã giảm giá");
    }

    Promotion promo = promotionRepository.findByCode(code)
        .orElse(null);

    if (promo == null) {
      return ApiResponse.error("Mã giảm giá không tồn tại");
    }

    if (!Boolean.TRUE.equals(promo.getIsActive())) {
      return ApiResponse.error("Mã giảm giá đã hết hiệu lực");
    }

    if (promo.getUsageLimit() != null && promo.getOrders() != null
        && promo.getOrders().size() >= promo.getUsageLimit()) {
      return ApiResponse.error("Mã giảm giá đã hết lượt sử dụng");
    }

    if (promo.getMinOrder() != null && orderTotal.compareTo(promo.getMinOrder()) < 0) {
      return ApiResponse.error("Đơn hàng tối thiểu " +
          String.format("%,.0f", promo.getMinOrder()) + "đ để sử dụng mã này");
    }

    // Calculate discount
    BigDecimal discountAmount;
    boolean isPercent = "PERCENT".equalsIgnoreCase(promo.getDiscountType())
        || "PERCENTAGE".equalsIgnoreCase(promo.getDiscountType());
    if (isPercent) {
      discountAmount = orderTotal.multiply(promo.getValue())
          .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
      if (promo.getMaxDiscount() != null && discountAmount.compareTo(promo.getMaxDiscount()) > 0) {
        discountAmount = promo.getMaxDiscount();
      }
    } else {
      discountAmount = promo.getValue();
    }

    // Don't exceed order total
    if (discountAmount.compareTo(orderTotal) > 0) {
      discountAmount = orderTotal;
    }

    Map<String, Object> result = new HashMap<>();
    result.put("code", promo.getCode());
    result.put("discountType", isPercent ? "PERCENTAGE" : "FIXED");
    result.put("value", promo.getValue());
    result.put("discountAmount", discountAmount);
    result.put("maxDiscount", promo.getMaxDiscount());
    result.put("minOrder", promo.getMinOrder());
    result.put("description", promo.getDescription() != null ? promo.getDescription() : "");

    return ApiResponse.success("Áp dụng mã giảm giá thành công", result);
  }
}
