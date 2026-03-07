package com.swp391.cclearly.dto.banner;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBannerRequest {

  @NotBlank(message = "URL ảnh không được để trống")
  private String imageUrl;

  @NotBlank(message = "Vị trí không được để trống")
  private String position;
}
