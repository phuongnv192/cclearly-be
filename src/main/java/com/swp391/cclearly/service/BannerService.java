package com.swp391.cclearly.service;

import com.swp391.cclearly.dto.banner.BannerResponse;
import com.swp391.cclearly.dto.banner.CreateBannerRequest;
import com.swp391.cclearly.dto.base.ApiResponse;
import com.swp391.cclearly.entity.ContentBanner;
import com.swp391.cclearly.exception.ResourceNotFoundException;
import com.swp391.cclearly.repository.ContentBannerRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

  private final ContentBannerRepository bannerRepository;

  public ApiResponse<List<BannerResponse>> getAllBanners() {
    List<BannerResponse> response = bannerRepository.findAll().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
    return ApiResponse.success("Lấy danh sách banner thành công", response);
  }

  public ApiResponse<BannerResponse> getBannerById(UUID id) {
    ContentBanner banner = bannerRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner"));
    return ApiResponse.success("Lấy thông tin banner thành công", toResponse(banner));
  }

  @Transactional
  public ApiResponse<BannerResponse> createBanner(CreateBannerRequest request) {
    ContentBanner banner = ContentBanner.builder()
        .imageUrl(request.getImageUrl())
        .position(request.getPosition())
        .build();

    banner = bannerRepository.save(banner);
    return ApiResponse.success("Tạo banner thành công", toResponse(banner));
  }

  @Transactional
  public ApiResponse<BannerResponse> updateBanner(UUID id, CreateBannerRequest request) {
    ContentBanner banner = bannerRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner"));

    if (request.getImageUrl() != null) banner.setImageUrl(request.getImageUrl());
    if (request.getPosition() != null) banner.setPosition(request.getPosition());

    bannerRepository.save(banner);
    return ApiResponse.success("Cập nhật banner thành công", toResponse(banner));
  }

  @Transactional
  public ApiResponse<Void> deleteBanner(UUID id) {
    ContentBanner banner = bannerRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner"));
    bannerRepository.delete(banner);
    return ApiResponse.success("Xóa banner thành công", null);
  }

  private BannerResponse toResponse(ContentBanner b) {
    return BannerResponse.builder()
        .bannerId(b.getBannerId())
        .imageUrl(b.getImageUrl())
        .position(b.getPosition())
        .build();
  }
}
