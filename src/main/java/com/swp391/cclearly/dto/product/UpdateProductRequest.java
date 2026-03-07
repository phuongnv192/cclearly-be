package com.swp391.cclearly.dto.product;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class UpdateProductRequest {

  private String name;
  private String type;
  private BigDecimal price;
  private String description;
  private Boolean isActive;

  // Frame-specific attributes
  private CreateProductRequest.FrameAttributes frameAttributes;

  // Lens-specific attributes
  private CreateProductRequest.LensAttributes lensAttributes;

  // Variants
  private List<CreateProductRequest.VariantRequest> variants;
}
