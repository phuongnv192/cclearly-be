package com.swp391.cclearly.service;

import com.swp391.cclearly.dto.base.ApiResponse;
import com.swp391.cclearly.dto.user.UpdateProfileRequest;
import com.swp391.cclearly.dto.user.UserProfileResponse;
import com.swp391.cclearly.entity.User;
import com.swp391.cclearly.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public ApiResponse<UserProfileResponse> getProfile(User user) {
    return ApiResponse.success("Lấy thông tin thành công", toResponse(user));
  }

  public ApiResponse<UserProfileResponse> updateProfile(User user, UpdateProfileRequest request) {
    if (request.getFullName() != null && !request.getFullName().isBlank()) {
      user.setFullName(request.getFullName());
    }
    if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
      user.setPhoneNumber(request.getPhoneNumber());
    }
    userRepository.save(user);
    return ApiResponse.success("Cập nhật thông tin thành công", toResponse(user));
  }

  private UserProfileResponse toResponse(User user) {
    return UserProfileResponse.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .phoneNumber(user.getPhoneNumber())
        .isEmailVerified(user.getIsEmailVerified())
        .role(user.getRole().getRoleName())
        .build();
  }
}
