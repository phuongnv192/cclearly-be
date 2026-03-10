package com.swp391.cclearly.controller;

import com.swp391.cclearly.dto.base.ApiResponse;
import com.swp391.cclearly.repository.SystemConfigRepository;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

  private final SystemConfigRepository systemConfigRepository;

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
}
