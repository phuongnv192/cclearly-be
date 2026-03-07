package com.swp391.cclearly.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class CreateProductRequest {

  @NotBlank(message = "Tên sản phẩm không được để trống")
  private String name;

  @NotBlank(message = "Loại sản phẩm không được để trống")
  private String type; // frame, lens, accessory

  @NotNull(message = "Giá cơ bản không được để trống")
  private BigDecimal price;

  private String description;

  // Frame-specific attributes
  private FrameAttributes frameAttributes;

  // Lens-specific attributes
  private LensAttributes lensAttributes;

  // Variants
  private List<VariantRequest> variants;

  @Data
  public static class FrameAttributes {
    private String material;
    private String shape;
    private String color;
    private Integer bridgeWidth;
    private Integer templeLength;
    private Integer lensWidth;
    private Integer frameWidth;
    private String origin;
    private String warranty;
  }

  @Data
  public static class LensAttributes {
    private String index;
    private String material;
    private String technology;
    private String coating;
    private String diameter;
    private String type;
    private String brand;
  }

  @Data
  public static class VariantRequest {
    private String sku;
    private String colorName;
    private BigDecimal salePrice;
    private Boolean isPreorder;
    private List<String> images;
  }
}
