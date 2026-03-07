package com.swp391.cclearly.controller;

import com.swp391.cclearly.dto.base.ApiResponse;
import com.swp391.cclearly.dto.order.OrderResponse;
import com.swp391.cclearly.dto.order.ReturnRequest;
import com.swp391.cclearly.dto.user.UpdateProfileRequest;
import com.swp391.cclearly.dto.user.UserProfileResponse;
import com.swp391.cclearly.entity.User;
import com.swp391.cclearly.service.OrderService;
import com.swp391.cclearly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "APIs quản lý thông tin người dùng")
public class UserController {

  private final UserService userService;
  private final OrderService orderService;

  @Operation(summary = "Lấy thông tin cá nhân")
  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(userService.getProfile(user));
  }

  @Operation(summary = "Cập nhật thông tin cá nhân")
  @PatchMapping("/profile")
  public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
      @AuthenticationPrincipal User user,
      @RequestBody UpdateProfileRequest request) {
    return ResponseEntity.ok(userService.updateProfile(user, request));
  }

  @Operation(summary = "Lấy danh sách đơn hàng của người dùng")
  @GetMapping("/orders")
  public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(orderService.getUserOrders(user));
  }

  @Operation(summary = "Yêu cầu trả hàng/hoàn tiền")
  @PostMapping("/orders/{orderId}/return")
  public ResponseEntity<ApiResponse<Void>> requestReturn(
      @AuthenticationPrincipal User user,
      @PathVariable UUID orderId,
      @Valid @RequestBody ReturnRequest request) {
    return ResponseEntity.ok(orderService.requestReturn(user, orderId, request));
  }
}
