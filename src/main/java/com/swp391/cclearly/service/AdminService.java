package com.swp391.cclearly.service;

import com.swp391.cclearly.dto.admin.AdminUserResponse;
import com.swp391.cclearly.dto.admin.AuditLogPageResponse;
import com.swp391.cclearly.dto.admin.AuditLogResponse;
import com.swp391.cclearly.dto.admin.CreateUserRequest;
import com.swp391.cclearly.dto.admin.DashboardStatsResponse;
import com.swp391.cclearly.dto.admin.RevenueResponse;
import com.swp391.cclearly.dto.admin.SystemSettingResponse;
import com.swp391.cclearly.dto.admin.UpdateSettingsRequest;
import com.swp391.cclearly.dto.admin.UpdateUserRequest;
import com.swp391.cclearly.dto.base.ApiResponse;
import com.swp391.cclearly.entity.AuditLog;
import com.swp391.cclearly.entity.Order;
import com.swp391.cclearly.entity.SystemConfig;
import com.swp391.cclearly.entity.User;
import com.swp391.cclearly.exception.BadRequestException;
import com.swp391.cclearly.exception.ResourceNotFoundException;
import com.swp391.cclearly.repository.AuditLogRepository;
import com.swp391.cclearly.repository.OrderRepository;
import com.swp391.cclearly.repository.ProductRepository;
import com.swp391.cclearly.repository.RoleRepository;
import com.swp391.cclearly.repository.SystemConfigRepository;
import com.swp391.cclearly.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final SystemConfigRepository systemConfigRepository;
  private final RoleRepository roleRepository;
  private final AuditLogRepository auditLogRepository;
  private final PasswordEncoder passwordEncoder;

  public ApiResponse<DashboardStatsResponse> getDashboardStats() {
    long totalOrders = orderRepository.count();
    long totalCustomers = userRepository.count();
    long totalProducts = productRepository.count();

    long pendingOrders = orderRepository.countByStatus("PENDING");
    long processingOrders = orderRepository.countByStatus("PROCESSING");
    long deliveredOrders = orderRepository.countByStatus("DELIVERED");
    long cancelledOrders = orderRepository.countByStatus("CANCELLED");

    // Total revenue from delivered orders
    List<Order> allOrders = orderRepository.findAll();
    BigDecimal totalRevenue = allOrders.stream()
        .filter(o -> "DELIVERED".equals(o.getStatus()))
        .map(Order::getFinalAmount)
        .filter(a -> a != null)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Orders by status
    Map<String, Long> ordersByStatus = new HashMap<>();
    ordersByStatus.put("PENDING", pendingOrders);
    ordersByStatus.put("PROCESSING", processingOrders);
    ordersByStatus.put("DELIVERED", deliveredOrders);
    ordersByStatus.put("CANCELLED", cancelledOrders);

    DashboardStatsResponse stats = DashboardStatsResponse.builder()
        .totalOrders(totalOrders)
        .totalCustomers(totalCustomers)
        .totalProducts(totalProducts)
        .totalRevenue(totalRevenue)
        .pendingOrders(pendingOrders)
        .processingOrders(processingOrders)
        .deliveredOrders(deliveredOrders)
        .cancelledOrders(cancelledOrders)
        .ordersByStatus(ordersByStatus)
        .revenueByMonth(new ArrayList<>())
        .topProducts(new ArrayList<>())
        .build();

    return ApiResponse.success("Lấy thống kê dashboard thành công", stats);
  }

  public ApiResponse<List<AdminUserResponse>> getAllUsers(
      String search, String role, int page, int size) {
    List<User> allUsers = userRepository.findAll();

    // Filter
    List<User> filtered = allUsers.stream()
        .filter(u -> {
          if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            return (u.getFullName() != null && u.getFullName().toLowerCase().contains(q))
                || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q));
          }
          return true;
        })
        .filter(u -> {
          if (role != null && !role.isBlank()) {
            return u.getRole() != null && role.equalsIgnoreCase(u.getRole().getRoleName());
          }
          return true;
        })
        .collect(Collectors.toList());

    // Paginate manually
    int start = (page - 1) * size;
    int end = Math.min(start + size, filtered.size());
    List<AdminUserResponse> response = (start < filtered.size())
        ? filtered.subList(start, end).stream().map(this::toAdminUserResponse).collect(Collectors.toList())
        : new ArrayList<>();

    return ApiResponse.success("Lấy danh sách người dùng thành công", response);
  }

  @Transactional
  public ApiResponse<AdminUserResponse> updateUser(UUID userId, UpdateUserRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

    if (request.getFullName() != null) user.setFullName(request.getFullName());
    if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
    if (request.getStatus() != null) user.setStatus(request.getStatus());

    if (request.getRole() != null) {
      roleRepository.findByRoleName(request.getRole())
          .ifPresent(user::setRole);
    }

    userRepository.save(user);
    return ApiResponse.success("Cập nhật người dùng thành công", toAdminUserResponse(user));
  }

  public ApiResponse<RevenueResponse> getRevenue() {
    List<Order> deliveredOrders = orderRepository.findAll().stream()
        .filter(o -> "DELIVERED".equals(o.getStatus()))
        .collect(Collectors.toList());

    BigDecimal totalRevenue = deliveredOrders.stream()
        .map(Order::getFinalAmount)
        .filter(a -> a != null)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // This/Last month revenue - simplified
    BigDecimal thisMonthRevenue = BigDecimal.ZERO;
    BigDecimal lastMonthRevenue = BigDecimal.ZERO;

    double growthPercent = 0.0;
    if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
      growthPercent = thisMonthRevenue.subtract(lastMonthRevenue)
          .divide(lastMonthRevenue, 4, RoundingMode.HALF_UP)
          .doubleValue() * 100;
    }

    RevenueResponse response = RevenueResponse.builder()
        .totalRevenue(totalRevenue)
        .thisMonthRevenue(thisMonthRevenue)
        .lastMonthRevenue(lastMonthRevenue)
        .growthPercent(growthPercent)
        .revenueByDay(new ArrayList<>())
        .build();

    return ApiResponse.success("Lấy doanh thu thành công", response);
  }

  public ApiResponse<List<SystemSettingResponse>> getSettings() {
    List<SystemConfig> configs = systemConfigRepository.findAll();
    List<SystemSettingResponse> response = configs.stream()
        .map(c -> SystemSettingResponse.builder()
            .key(c.getConfigKey())
            .value(c.getConfigValue())
            .group(c.getConfigGroup())
            .build())
        .collect(Collectors.toList());
    return ApiResponse.success("Lấy cấu hình hệ thống thành công", response);
  }

  @Transactional
  public ApiResponse<List<SystemSettingResponse>> updateSettings(UpdateSettingsRequest request) {
    if (request.getSettings() != null) {
      for (var entry : request.getSettings().entrySet()) {
        SystemConfig config = systemConfigRepository.findByConfigKey(entry.getKey())
            .orElse(SystemConfig.builder()
                .configKey(entry.getKey())
                .configGroup("general")
                .build());
        config.setConfigValue(entry.getValue());
        systemConfigRepository.save(config);
      }
    }
    return getSettings();
  }

  /**
   * Tạo tài khoản người dùng mới (nhân viên hoặc khách hàng)
   * Dùng cho: StaffPage "Thêm nhân viên", RolePermissionPage "Thêm nhân sự mới"
   */
  @Transactional
  public ApiResponse<AdminUserResponse> createUser(CreateUserRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BadRequestException("Email đã được sử dụng");
    }

    if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()
        && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
      throw new BadRequestException("Số điện thoại đã được sử dụng");
    }

    var role = roleRepository.findByRoleName(request.getRole().toUpperCase())
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò: " + request.getRole()));

    User user = User.builder()
        .email(request.getEmail())
        .passwordHash(passwordEncoder.encode(request.getPassword()))
        .fullName(request.getFullName())
        .phoneNumber(request.getPhoneNumber())
        .role(role)
        .status("ACTIVE")
        .isEmailVerified(true) // Admin creates => auto verified
        .createdAt(Instant.now())
        .build();

    user = userRepository.save(user);
    return ApiResponse.success("Tạo tài khoản thành công", toAdminUserResponse(user));
  }

  /**
   * Xóa (vô hiệu hóa) tài khoản người dùng
   * Soft delete: set status = INACTIVE
   * Dùng cho: StaffPage "Xóa", RolePermissionPage
   */
  @Transactional
  public ApiResponse<Void> deleteUser(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

    user.setStatus("INACTIVE");
    userRepository.save(user);

    return ApiResponse.success("Đã vô hiệu hóa tài khoản", null);
  }

  /**
   * Lấy danh sách nhật ký hệ thống (audit logs)
   * Dùng cho: SystemLogsPage, RolePermissionPage "Audit Log" tab
   */
  public ApiResponse<AuditLogPageResponse> getAuditLogs(String action, int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    Page<AuditLog> logPage;

    if (action != null && !action.isBlank()) {
      logPage = auditLogRepository.findByActionContainingIgnoreCaseOrderByLogIdDesc(action, pageable);
    } else {
      logPage = auditLogRepository.findAllByOrderByLogIdDesc(pageable);
    }

    List<AuditLogResponse> items = logPage.getContent().stream()
        .map(log -> AuditLogResponse.builder()
            .logId(log.getLogId())
            .userId(log.getUser() != null ? log.getUser().getUserId().toString() : null)
            .userName(log.getUser() != null ? log.getUser().getFullName() : "System")
            .action(log.getAction())
            .oldValue(log.getOldValue())
            .newValue(log.getNewValue())
            .details(log.getOldValue() != null
                ? "Cũ: " + log.getOldValue() + " → Mới: " + log.getNewValue()
                : log.getNewValue())
            .build())
        .collect(Collectors.toList());

    AuditLogPageResponse response = AuditLogPageResponse.builder()
        .items(items)
        .meta(AuditLogPageResponse.Meta.builder()
            .page(page)
            .size(size)
            .totalElements(logPage.getTotalElements())
            .totalPages(logPage.getTotalPages())
            .build())
        .build();

    return ApiResponse.success("Lấy nhật ký hệ thống thành công", response);
  }

  private AdminUserResponse toAdminUserResponse(User user) {
    return AdminUserResponse.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .phoneNumber(user.getPhoneNumber())
        .role(user.getRole() != null ? user.getRole().getRoleName() : null)
        .status(user.getStatus())
        .isEmailVerified(user.getIsEmailVerified())
        .createdAt(user.getCreatedAt())
        .lastLogin(user.getLastLogin())
        .build();
  }
}
