package com.swp391.cclearly.service;

import com.swp391.cclearly.dto.base.ApiResponse;
import com.swp391.cclearly.dto.product.CreateProductRequest;
import com.swp391.cclearly.dto.product.ProductPageResponse;
import com.swp391.cclearly.dto.product.ProductResponse;
import com.swp391.cclearly.dto.product.UpdateProductRequest;
import com.swp391.cclearly.entity.Product;
import com.swp391.cclearly.entity.ProductFrame;
import com.swp391.cclearly.entity.ProductImage;
import com.swp391.cclearly.entity.ProductLens;
import com.swp391.cclearly.entity.ProductVariant;
import com.swp391.cclearly.exception.ResourceNotFoundException;
import com.swp391.cclearly.repository.ProductImageRepository;
import com.swp391.cclearly.repository.ProductRepository;
import com.swp391.cclearly.repository.ProductVariantRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductVariantRepository productVariantRepository;
  private final ProductImageRepository productImageRepository;

  public ApiResponse<ProductPageResponse> getProducts(String type, String search, int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);

    Specification<Product> spec = Specification.where(activeOnly());
    if (type != null && !type.isBlank()) {
      spec = spec.and(hasType(type));
    }
    if (search != null && !search.isBlank()) {
      spec = spec.and(nameContains(search));
    }

    Page<Product> productPage = productRepository.findAll(spec, pageable);
    List<ProductResponse> content = productPage.getContent().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());

    ProductPageResponse response = ProductPageResponse.builder()
        .content(content)
        .page(page)
        .size(size)
        .totalElements(productPage.getTotalElements())
        .totalPages(productPage.getTotalPages())
        .build();

    return ApiResponse.success("Lấy danh sách sản phẩm thành công", response);
  }

  public ApiResponse<ProductResponse> getProductById(UUID id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
    return ApiResponse.success("Lấy thông tin sản phẩm thành công", toResponse(product));
  }

  public ApiResponse<List<Map<String, Object>>> getCategories() {
    List<Map<String, Object>> categories = List.of(
        Map.of("id", "frame", "name", "Gọng kính", "type", "frame"),
        Map.of("id", "lens", "name", "Tròng kính", "type", "lens"),
        Map.of("id", "accessory", "name", "Phụ kiện", "type", "accessory")
    );
    return ApiResponse.success("Lấy danh sách danh mục thành công", categories);
  }

  @Transactional
  public ApiResponse<ProductResponse> createProduct(CreateProductRequest request) {
    Product product = Product.builder()
        .name(request.getName())
        .categoryType(request.getType())
        .basePrice(request.getPrice())
        .isActive(true)
        .variants(new HashSet<>())
        .images(new HashSet<>())
        .build();

    // Frame attributes
    if ("frame".equals(request.getType()) && request.getFrameAttributes() != null) {
      var fa = request.getFrameAttributes();
      ProductFrame frame = ProductFrame.builder()
          .product(product)
          .material(fa.getMaterial())
          .shape(fa.getShape())
          .totalWidthMm(fa.getFrameWidth())
          .lensWidthMm(fa.getLensWidth())
          .bridgeWidthMm(fa.getBridgeWidth())
          .build();
      product.setProductFrame(frame);
    }

    // Lens attributes
    if ("lens".equals(request.getType()) && request.getLensAttributes() != null) {
      var la = request.getLensAttributes();
      ProductLens lens = ProductLens.builder()
          .product(product)
          .lensType(la.getType())
          .build();
      product.setProductLens(lens);
    }

    product = productRepository.save(product);

    // Variants
    if (request.getVariants() != null) {
      for (var vr : request.getVariants()) {
        ProductVariant variant = ProductVariant.builder()
            .product(product)
            .sku(vr.getSku())
            .colorName(vr.getColorName())
            .salePrice(vr.getSalePrice())
            .isPreorder(vr.getIsPreorder() != null ? vr.getIsPreorder() : false)
            .images(new HashSet<>())
            .build();
        variant = productVariantRepository.save(variant);

        if (vr.getImages() != null) {
          for (String imgUrl : vr.getImages()) {
            ProductImage img = ProductImage.builder()
                .product(product)
                .variant(variant)
                .imageUrl(imgUrl)
                .build();
            productImageRepository.save(img);
          }
        }
        product.getVariants().add(variant);
      }
    }

    return ApiResponse.success("Tạo sản phẩm thành công", toResponse(product));
  }

  @Transactional
  public ApiResponse<ProductResponse> updateProduct(UUID id, UpdateProductRequest request) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

    if (request.getName() != null) product.setName(request.getName());
    if (request.getType() != null) product.setCategoryType(request.getType());
    if (request.getPrice() != null) product.setBasePrice(request.getPrice());
    if (request.getIsActive() != null) product.setIsActive(request.getIsActive());

    // Update frame attributes
    if (request.getFrameAttributes() != null) {
      var fa = request.getFrameAttributes();
      ProductFrame frame = product.getProductFrame();
      if (frame == null) {
        frame = ProductFrame.builder().product(product).build();
        product.setProductFrame(frame);
      }
      if (fa.getMaterial() != null) frame.setMaterial(fa.getMaterial());
      if (fa.getShape() != null) frame.setShape(fa.getShape());
      if (fa.getFrameWidth() != null) frame.setTotalWidthMm(fa.getFrameWidth());
      if (fa.getLensWidth() != null) frame.setLensWidthMm(fa.getLensWidth());
      if (fa.getBridgeWidth() != null) frame.setBridgeWidthMm(fa.getBridgeWidth());
    }

    // Update lens attributes
    if (request.getLensAttributes() != null) {
      var la = request.getLensAttributes();
      ProductLens lens = product.getProductLens();
      if (lens == null) {
        lens = ProductLens.builder().product(product).build();
        product.setProductLens(lens);
      }
      if (la.getType() != null) lens.setLensType(la.getType());
    }

    productRepository.save(product);
    return ApiResponse.success("Cập nhật sản phẩm thành công", toResponse(product));
  }

  @Transactional
  public ApiResponse<Void> deleteProduct(UUID id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
    product.setIsActive(false);
    productRepository.save(product);
    return ApiResponse.success("Xóa sản phẩm thành công", null);
  }

  private ProductResponse toResponse(Product p) {
    // Collect product-level images
    List<String> images = p.getImages().stream()
        .filter(img -> img.getVariant() == null)
        .map(ProductImage::getImageUrl)
        .collect(Collectors.toList());

    // If no product-level images, take first variant images
    if (images.isEmpty() && !p.getVariants().isEmpty()) {
      images = p.getVariants().iterator().next().getImages().stream()
          .map(ProductImage::getImageUrl)
          .limit(3)
          .collect(Collectors.toList());
    }

    // Check if any variant has a lower salePrice (for isSale flag)
    boolean isSale = p.getVariants().stream()
        .anyMatch(v -> v.getSalePrice() != null && v.getSalePrice().compareTo(p.getBasePrice()) < 0);
    var minSalePrice = p.getVariants().stream()
        .filter(v -> v.getSalePrice() != null)
        .map(ProductVariant::getSalePrice)
        .min(java.math.BigDecimal::compareTo)
        .orElse(p.getBasePrice());

    // Variants
    List<ProductResponse.VariantInfo> variants = p.getVariants().stream()
        .map(v -> ProductResponse.VariantInfo.builder()
            .variantId(v.getVariantId())
            .sku(v.getSku())
            .colorName(v.getColorName())
            .salePrice(v.getSalePrice())
            .isPreorder(v.getIsPreorder())
            .images(v.getImages().stream().map(ProductImage::getImageUrl).collect(Collectors.toList()))
            .build())
        .collect(Collectors.toList());

    ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
        .id(p.getProductId())
        .name(p.getName())
        .type(p.getCategoryType())
        .basePrice(p.getBasePrice())
        .isActive(p.getIsActive())
        .isSale(isSale)
        .salePrice(isSale ? minSalePrice : null)
        .images(images)
        .variants(variants);

    if (p.getProductFrame() != null) {
      var f = p.getProductFrame();
      builder.frame(ProductResponse.FrameInfo.builder()
          .material(f.getMaterial())
          .shape(f.getShape())
          .totalWidthMm(f.getTotalWidthMm())
          .lensWidthMm(f.getLensWidthMm())
          .bridgeWidthMm(f.getBridgeWidthMm())
          .build());
    }

    if (p.getProductLens() != null) {
      var l = p.getProductLens();
      builder.lens(ProductResponse.LensInfo.builder()
          .lensType(l.getLensType())
          .build());
    }

    return builder.build();
  }

  private static Specification<Product> activeOnly() {
    return (root, query, cb) -> cb.equal(root.get("isActive"), true);
  }

  private static Specification<Product> hasType(String type) {
    return (root, query, cb) -> cb.equal(root.get("categoryType"), type);
  }

  private static Specification<Product> nameContains(String search) {
    return (root, query, cb) ->
        cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
  }
}
