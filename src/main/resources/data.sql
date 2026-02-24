-- =============================================
-- CCLEARLY DATABASE SEED DATA
-- Auto-run by Spring Boot on startup
-- Password: Abc@12345 (BCrypt encoded)
-- =============================================

-- =============================================
-- 1. ROLES
-- =============================================
IF NOT EXISTS (SELECT * FROM [Roles] WHERE role_name = 'ADMIN')
    INSERT INTO [Roles] (role_id, role_name, description) VALUES (NEWID(), 'ADMIN', N'Quản trị viên hệ thống');

IF NOT EXISTS (SELECT * FROM [Roles] WHERE role_name = 'MANAGER')
    INSERT INTO [Roles] (role_id, role_name, description) VALUES (NEWID(), 'MANAGER', N'Quản lý cửa hàng');

IF NOT EXISTS (SELECT * FROM [Roles] WHERE role_name = 'SALES_STAFF')
    INSERT INTO [Roles] (role_id, role_name, description) VALUES (NEWID(), 'SALES_STAFF', N'Nhân viên bán hàng');

IF NOT EXISTS (SELECT * FROM [Roles] WHERE role_name = 'OPERATION_STAFF')
    INSERT INTO [Roles] (role_id, role_name, description) VALUES (NEWID(), 'OPERATION_STAFF', N'Nhân viên vận hành');

IF NOT EXISTS (SELECT * FROM [Roles] WHERE role_name = 'CUSTOMER')
    INSERT INTO [Roles] (role_id, role_name, description) VALUES (NEWID(), 'CUSTOMER', N'Khách hàng');

-- =============================================
-- 2. PERMISSIONS
-- =============================================
IF NOT EXISTS (SELECT * FROM [Permissions] WHERE slug = 'USER_READ')
    INSERT INTO [Permissions] (permission_id, slug, description) VALUES (NEWID(), 'USER_READ', N'Xem thông tin người dùng');

IF NOT EXISTS (SELECT * FROM [Permissions] WHERE slug = 'USER_WRITE')
    INSERT INTO [Permissions] (permission_id, slug, description) VALUES (NEWID(), 'USER_WRITE', N'Chỉnh sửa thông tin người dùng');

IF NOT EXISTS (SELECT * FROM [Permissions] WHERE slug = 'PRODUCT_READ')
    INSERT INTO [Permissions] (permission_id, slug, description) VALUES (NEWID(), 'PRODUCT_READ', N'Xem sản phẩm');

IF NOT EXISTS (SELECT * FROM [Permissions] WHERE slug = 'PRODUCT_WRITE')
    INSERT INTO [Permissions] (permission_id, slug, description) VALUES (NEWID(), 'PRODUCT_WRITE', N'Thêm/sửa/xóa sản phẩm');

IF NOT EXISTS (SELECT * FROM [Permissions] WHERE slug = 'ORDER_READ')
    INSERT INTO [Permissions] (permission_id, slug, description) VALUES (NEWID(), 'ORDER_READ', N'Xem đơn hàng');

IF NOT EXISTS (SELECT * FROM [Permissions] WHERE slug = 'ORDER_WRITE')
    INSERT INTO [Permissions] (permission_id, slug, description) VALUES (NEWID(), 'ORDER_WRITE', N'Xử lý đơn hàng');

IF NOT EXISTS (SELECT * FROM [Permissions] WHERE slug = 'INVENTORY_READ')
    INSERT INTO [Permissions] (permission_id, slug, description) VALUES (NEWID(), 'INVENTORY_READ', N'Xem tồn kho');

IF NOT EXISTS (SELECT * FROM [Permissions] WHERE slug = 'INVENTORY_WRITE')
    INSERT INTO [Permissions] (permission_id, slug, description) VALUES (NEWID(), 'INVENTORY_WRITE', N'Quản lý tồn kho');

IF NOT EXISTS (SELECT * FROM [Permissions] WHERE slug = 'REPORT_READ')
    INSERT INTO [Permissions] (permission_id, slug, description) VALUES (NEWID(), 'REPORT_READ', N'Xem báo cáo');

IF NOT EXISTS (SELECT * FROM [Permissions] WHERE slug = 'CONFIG_WRITE')
    INSERT INTO [Permissions] (permission_id, slug, description) VALUES (NEWID(), 'CONFIG_WRITE', N'Cấu hình hệ thống');

-- =============================================
-- 3. ROLE_PERMISSIONS
-- =============================================
-- ADMIN gets all permissions
INSERT INTO [Role_Permissions] (role_id, permission_id)
SELECT r.role_id, p.permission_id 
FROM [Roles] r, [Permissions] p 
WHERE r.role_name = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM [Role_Permissions] rp WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id);

-- MANAGER gets all permissions except CONFIG_WRITE
INSERT INTO [Role_Permissions] (role_id, permission_id)
SELECT r.role_id, p.permission_id 
FROM [Roles] r, [Permissions] p 
WHERE r.role_name = 'MANAGER' AND p.slug != 'CONFIG_WRITE'
AND NOT EXISTS (SELECT 1 FROM [Role_Permissions] rp WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id);

-- SALES_STAFF: product, order
INSERT INTO [Role_Permissions] (role_id, permission_id)
SELECT r.role_id, p.permission_id 
FROM [Roles] r, [Permissions] p 
WHERE r.role_name = 'SALES_STAFF' AND p.slug IN ('PRODUCT_READ', 'PRODUCT_WRITE', 'ORDER_READ', 'ORDER_WRITE')
AND NOT EXISTS (SELECT 1 FROM [Role_Permissions] rp WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id);

-- OPERATION_STAFF: inventory, order
INSERT INTO [Role_Permissions] (role_id, permission_id)
SELECT r.role_id, p.permission_id 
FROM [Roles] r, [Permissions] p 
WHERE r.role_name = 'OPERATION_STAFF' AND p.slug IN ('INVENTORY_READ', 'INVENTORY_WRITE', 'ORDER_READ', 'ORDER_WRITE')
AND NOT EXISTS (SELECT 1 FROM [Role_Permissions] rp WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id);

-- =============================================
-- 4. USERS (Password: Abc@12345)
-- BCrypt hash generated with strength 10
-- =============================================
IF NOT EXISTS (SELECT * FROM [Users] WHERE email = 'admin@cclearly.com')
    INSERT INTO [Users] (user_id, email, password_hash, full_name, phone_number, role_id, status, is_email_verified, created_at)
    SELECT NEWID(), 'admin@cclearly.com', '$2a$10$QzSf/hgRoo3mkItxEyUDs.xCgnn7uacO.FDHTEZnNDACHrXhq08SK', N'Admin CClearly', '0901234567', role_id, 'ACTIVE', 1, GETUTCDATE()
    FROM [Roles] WHERE role_name = 'ADMIN';

IF NOT EXISTS (SELECT * FROM [Users] WHERE email = 'manager@cclearly.com')
    INSERT INTO [Users] (user_id, email, password_hash, full_name, phone_number, role_id, status, is_email_verified, created_at)
    SELECT NEWID(), 'manager@cclearly.com', '$2a$10$QzSf/hgRoo3mkItxEyUDs.xCgnn7uacO.FDHTEZnNDACHrXhq08SK', N'Nguyễn Văn Quản Lý', '0901234568', role_id, 'ACTIVE', 1, GETUTCDATE()
    FROM [Roles] WHERE role_name = 'MANAGER';

IF NOT EXISTS (SELECT * FROM [Users] WHERE email = 'sales@cclearly.com')
    INSERT INTO [Users] (user_id, email, password_hash, full_name, phone_number, role_id, status, is_email_verified, created_at)
    SELECT NEWID(), 'sales@cclearly.com', '$2a$10$QzSf/hgRoo3mkItxEyUDs.xCgnn7uacO.FDHTEZnNDACHrXhq08SK', N'Trần Thị Bán Hàng', '0901234569', role_id, 'ACTIVE', 1, GETUTCDATE()
    FROM [Roles] WHERE role_name = 'SALES_STAFF';

IF NOT EXISTS (SELECT * FROM [Users] WHERE email = 'operation@cclearly.com')
    INSERT INTO [Users] (user_id, email, password_hash, full_name, phone_number, role_id, status, is_email_verified, created_at)
    SELECT NEWID(), 'operation@cclearly.com', '$2a$10$QzSf/hgRoo3mkItxEyUDs.xCgnn7uacO.FDHTEZnNDACHrXhq08SK', N'Lê Văn Vận Hành', '0901234570', role_id, 'ACTIVE', 1, GETUTCDATE()
    FROM [Roles] WHERE role_name = 'OPERATION_STAFF';

IF NOT EXISTS (SELECT * FROM [Users] WHERE email = 'customer1@gmail.com')
    INSERT INTO [Users] (user_id, email, password_hash, full_name, phone_number, role_id, status, is_email_verified, created_at)
    SELECT NEWID(), 'customer1@gmail.com', '$2a$10$QzSf/hgRoo3mkItxEyUDs.xCgnn7uacO.FDHTEZnNDACHrXhq08SK', N'Phạm Văn Khách 1', '0912345678', role_id, 'ACTIVE', 1, GETUTCDATE()
    FROM [Roles] WHERE role_name = 'CUSTOMER';

IF NOT EXISTS (SELECT * FROM [Users] WHERE email = 'customer2@gmail.com')
    INSERT INTO [Users] (user_id, email, password_hash, full_name, phone_number, role_id, status, is_email_verified, created_at)
    SELECT NEWID(), 'customer2@gmail.com', '$2a$10$QzSf/hgRoo3mkItxEyUDs.xCgnn7uacO.FDHTEZnNDACHrXhq08SK', N'Hoàng Thị Khách 2', '0912345679', role_id, 'ACTIVE', 1, GETUTCDATE()
    FROM [Roles] WHERE role_name = 'CUSTOMER';

-- =============================================
-- 5. STAFF_PROFILES
-- =============================================
INSERT INTO [Staff_Profiles] (user_id)
SELECT user_id FROM [Users] WHERE email = 'manager@cclearly.com'
AND NOT EXISTS (SELECT 1 FROM [Staff_Profiles] sp WHERE sp.user_id = Users.user_id);

INSERT INTO [Staff_Profiles] (user_id)
SELECT user_id FROM [Users] WHERE email = 'sales@cclearly.com'
AND NOT EXISTS (SELECT 1 FROM [Staff_Profiles] sp WHERE sp.user_id = Users.user_id);

INSERT INTO [Staff_Profiles] (user_id)
SELECT user_id FROM [Users] WHERE email = 'operation@cclearly.com'
AND NOT EXISTS (SELECT 1 FROM [Staff_Profiles] sp WHERE sp.user_id = Users.user_id);

-- =============================================
-- 6. WAREHOUSES
-- =============================================
IF NOT EXISTS (SELECT * FROM [Warehouses] WHERE name = N'Kho chính Hà Nội')
    INSERT INTO [Warehouses] (warehouse_id, name) VALUES (NEWID(), N'Kho chính Hà Nội');

IF NOT EXISTS (SELECT * FROM [Warehouses] WHERE name = N'Kho TP.HCM')
    INSERT INTO [Warehouses] (warehouse_id, name) VALUES (NEWID(), N'Kho TP.HCM');

-- =============================================
-- 7. MASTER_LENS_TECHNOLOGIES
-- =============================================
IF NOT EXISTS (SELECT * FROM [Master_Lens_Technologies] WHERE name = N'Lọc ánh sáng xanh')
    INSERT INTO [Master_Lens_Technologies] (tech_id, name) VALUES (NEWID(), N'Lọc ánh sáng xanh');

IF NOT EXISTS (SELECT * FROM [Master_Lens_Technologies] WHERE name = N'Đổi màu')
    INSERT INTO [Master_Lens_Technologies] (tech_id, name) VALUES (NEWID(), N'Đổi màu');

IF NOT EXISTS (SELECT * FROM [Master_Lens_Technologies] WHERE name = N'Phân cực')
    INSERT INTO [Master_Lens_Technologies] (tech_id, name) VALUES (NEWID(), N'Phân cực');

IF NOT EXISTS (SELECT * FROM [Master_Lens_Technologies] WHERE name = N'Chống UV')
    INSERT INTO [Master_Lens_Technologies] (tech_id, name) VALUES (NEWID(), N'Chống UV');

IF NOT EXISTS (SELECT * FROM [Master_Lens_Technologies] WHERE name = N'Chống trầy')
    INSERT INTO [Master_Lens_Technologies] (tech_id, name) VALUES (NEWID(), N'Chống trầy');

-- =============================================
-- 8. PRODUCTS - FRAMES
-- =============================================
IF NOT EXISTS (SELECT * FROM [Products] WHERE name = N'Ray-Ban Aviator Classic')
BEGIN
    DECLARE @p1 UNIQUEIDENTIFIER = NEWID();
    INSERT INTO [Products] (product_id, name, category_type, base_price, is_active) VALUES (@p1, N'Ray-Ban Aviator Classic', 'FRAME', 3500000, 1);
    INSERT INTO [Product_Frames] (product_id, material, shape, total_width_mm, lens_width_mm, bridge_width_mm) VALUES (@p1, N'Kim loại', N'Phi công', 140, 58, 14);
END;

IF NOT EXISTS (SELECT * FROM [Products] WHERE name = N'Oakley Holbrook')
BEGIN
    DECLARE @p2 UNIQUEIDENTIFIER = NEWID();
    INSERT INTO [Products] (product_id, name, category_type, base_price, is_active) VALUES (@p2, N'Oakley Holbrook', 'FRAME', 2800000, 1);
    INSERT INTO [Product_Frames] (product_id, material, shape, total_width_mm, lens_width_mm, bridge_width_mm) VALUES (@p2, N'Nhựa', N'Vuông', 142, 55, 18);
END;

IF NOT EXISTS (SELECT * FROM [Products] WHERE name = N'Gucci GG0061S')
BEGIN
    DECLARE @p3 UNIQUEIDENTIFIER = NEWID();
    INSERT INTO [Products] (product_id, name, category_type, base_price, is_active) VALUES (@p3, N'Gucci GG0061S', 'FRAME', 7500000, 1);
    INSERT INTO [Product_Frames] (product_id, material, shape, total_width_mm, lens_width_mm, bridge_width_mm) VALUES (@p3, N'Acetate', N'Mắt mèo', 138, 56, 15);
END;

IF NOT EXISTS (SELECT * FROM [Products] WHERE name = N'Tommy Hilfiger TH1794')
BEGIN
    DECLARE @p4 UNIQUEIDENTIFIER = NEWID();
    INSERT INTO [Products] (product_id, name, category_type, base_price, is_active) VALUES (@p4, N'Tommy Hilfiger TH1794', 'FRAME', 1800000, 1);
    INSERT INTO [Product_Frames] (product_id, material, shape, total_width_mm, lens_width_mm, bridge_width_mm) VALUES (@p4, N'Kim loại', N'Chữ nhật', 136, 52, 17);
END;

-- =============================================
-- 9. PRODUCTS - LENSES
-- =============================================
IF NOT EXISTS (SELECT * FROM [Products] WHERE name = N'Essilor Crizal Sapphire')
BEGIN
    DECLARE @l1 UNIQUEIDENTIFIER = NEWID();
    INSERT INTO [Products] (product_id, name, category_type, base_price, is_active) VALUES (@l1, N'Essilor Crizal Sapphire', 'LENS', 2500000, 1);
    INSERT INTO [Product_Lenses] (product_id, refractive_index, lens_type) VALUES (@l1, 1.6, N'Đơn tròng');
END;

IF NOT EXISTS (SELECT * FROM [Products] WHERE name = N'Zeiss Digital Lens')
BEGIN
    DECLARE @l2 UNIQUEIDENTIFIER = NEWID();
    INSERT INTO [Products] (product_id, name, category_type, base_price, is_active) VALUES (@l2, N'Zeiss Digital Lens', 'LENS', 4500000, 1);
    INSERT INTO [Product_Lenses] (product_id, refractive_index, lens_type) VALUES (@l2, 1.67, N'Đa tròng lũy tiến');
END;

IF NOT EXISTS (SELECT * FROM [Products] WHERE name = N'Hoya Blue Control')
BEGIN
    DECLARE @l3 UNIQUEIDENTIFIER = NEWID();
    INSERT INTO [Products] (product_id, name, category_type, base_price, is_active) VALUES (@l3, N'Hoya Blue Control', 'LENS', 1800000, 1);
    INSERT INTO [Product_Lenses] (product_id, refractive_index, lens_type) VALUES (@l3, 1.6, N'Đơn tròng');
END;

-- =============================================
-- 10. PRODUCT_VARIANTS
-- =============================================
IF NOT EXISTS (SELECT * FROM [Product_Variants] WHERE sku = 'RB-AV-GOLD')
    INSERT INTO [Product_Variants] (variant_id, product_id, sku, color_name, sale_price, is_preorder)
    SELECT NEWID(), product_id, 'RB-AV-GOLD', N'Vàng', 3500000, 0 FROM [Products] WHERE name = N'Ray-Ban Aviator Classic';

IF NOT EXISTS (SELECT * FROM [Product_Variants] WHERE sku = 'RB-AV-SILVER')
    INSERT INTO [Product_Variants] (variant_id, product_id, sku, color_name, sale_price, is_preorder)
    SELECT NEWID(), product_id, 'RB-AV-SILVER', N'Bạc', 3500000, 0 FROM [Products] WHERE name = N'Ray-Ban Aviator Classic';

IF NOT EXISTS (SELECT * FROM [Product_Variants] WHERE sku = 'RB-AV-BLACK')
    INSERT INTO [Product_Variants] (variant_id, product_id, sku, color_name, sale_price, is_preorder)
    SELECT NEWID(), product_id, 'RB-AV-BLACK', N'Đen', 3700000, 0 FROM [Products] WHERE name = N'Ray-Ban Aviator Classic';

IF NOT EXISTS (SELECT * FROM [Product_Variants] WHERE sku = 'OAK-HB-BLACK')
    INSERT INTO [Product_Variants] (variant_id, product_id, sku, color_name, sale_price, is_preorder)
    SELECT NEWID(), product_id, 'OAK-HB-BLACK', N'Đen mờ', 2800000, 0 FROM [Products] WHERE name = N'Oakley Holbrook';

IF NOT EXISTS (SELECT * FROM [Product_Variants] WHERE sku = 'OAK-HB-TORT')
    INSERT INTO [Product_Variants] (variant_id, product_id, sku, color_name, sale_price, is_preorder)
    SELECT NEWID(), product_id, 'OAK-HB-TORT', N'Đồi mồi', 2900000, 0 FROM [Products] WHERE name = N'Oakley Holbrook';

IF NOT EXISTS (SELECT * FROM [Product_Variants] WHERE sku = 'GG-0061-BLACK')
    INSERT INTO [Product_Variants] (variant_id, product_id, sku, color_name, sale_price, is_preorder)
    SELECT NEWID(), product_id, 'GG-0061-BLACK', N'Đen/Vàng', 7500000, 0 FROM [Products] WHERE name = N'Gucci GG0061S';

-- =============================================
-- 11. PRODUCT_IMAGES
-- =============================================
INSERT INTO [Product_Images] (image_id, product_id, variant_id, image_url)
SELECT NEWID(), p.product_id, v.variant_id, 'https://res.cloudinary.com/cclearly/image/upload/v1/products/rayban-aviator-gold.jpg'
FROM [Products] p JOIN [Product_Variants] v ON p.product_id = v.product_id
WHERE v.sku = 'RB-AV-GOLD'
AND NOT EXISTS (SELECT 1 FROM [Product_Images] pi WHERE pi.variant_id = v.variant_id);

INSERT INTO [Product_Images] (image_id, product_id, variant_id, image_url)
SELECT NEWID(), p.product_id, v.variant_id, 'https://res.cloudinary.com/cclearly/image/upload/v1/products/rayban-aviator-silver.jpg'
FROM [Products] p JOIN [Product_Variants] v ON p.product_id = v.product_id
WHERE v.sku = 'RB-AV-SILVER'
AND NOT EXISTS (SELECT 1 FROM [Product_Images] pi WHERE pi.variant_id = v.variant_id);

INSERT INTO [Product_Images] (image_id, product_id, variant_id, image_url)
SELECT NEWID(), p.product_id, v.variant_id, 'https://res.cloudinary.com/cclearly/image/upload/v1/products/oakley-holbrook-black.jpg'
FROM [Products] p JOIN [Product_Variants] v ON p.product_id = v.product_id
WHERE v.sku = 'OAK-HB-BLACK'
AND NOT EXISTS (SELECT 1 FROM [Product_Images] pi WHERE pi.variant_id = v.variant_id);

INSERT INTO [Product_Images] (image_id, product_id, variant_id, image_url)
SELECT NEWID(), p.product_id, v.variant_id, 'https://res.cloudinary.com/cclearly/image/upload/v1/products/gucci-cateye.jpg'
FROM [Products] p JOIN [Product_Variants] v ON p.product_id = v.product_id
WHERE v.sku = 'GG-0061-BLACK'
AND NOT EXISTS (SELECT 1 FROM [Product_Images] pi WHERE pi.variant_id = v.variant_id);

-- =============================================
-- 12. INVENTORY_STOCK
-- =============================================
INSERT INTO [Inventory_Stock] (variant_id, warehouse_id, quantity_on_hand)
SELECT v.variant_id, w.warehouse_id, 50
FROM [Product_Variants] v, [Warehouses] w
WHERE v.sku = 'RB-AV-GOLD' AND w.name = N'Kho chính Hà Nội'
AND NOT EXISTS (SELECT 1 FROM [Inventory_Stock] s WHERE s.variant_id = v.variant_id AND s.warehouse_id = w.warehouse_id);

INSERT INTO [Inventory_Stock] (variant_id, warehouse_id, quantity_on_hand)
SELECT v.variant_id, w.warehouse_id, 30
FROM [Product_Variants] v, [Warehouses] w
WHERE v.sku = 'RB-AV-SILVER' AND w.name = N'Kho chính Hà Nội'
AND NOT EXISTS (SELECT 1 FROM [Inventory_Stock] s WHERE s.variant_id = v.variant_id AND s.warehouse_id = w.warehouse_id);

INSERT INTO [Inventory_Stock] (variant_id, warehouse_id, quantity_on_hand)
SELECT v.variant_id, w.warehouse_id, 25
FROM [Product_Variants] v, [Warehouses] w
WHERE v.sku = 'RB-AV-BLACK' AND w.name = N'Kho chính Hà Nội'
AND NOT EXISTS (SELECT 1 FROM [Inventory_Stock] s WHERE s.variant_id = v.variant_id AND s.warehouse_id = w.warehouse_id);

INSERT INTO [Inventory_Stock] (variant_id, warehouse_id, quantity_on_hand)
SELECT v.variant_id, w.warehouse_id, 40
FROM [Product_Variants] v, [Warehouses] w
WHERE v.sku = 'OAK-HB-BLACK' AND w.name = N'Kho chính Hà Nội'
AND NOT EXISTS (SELECT 1 FROM [Inventory_Stock] s WHERE s.variant_id = v.variant_id AND s.warehouse_id = w.warehouse_id);

INSERT INTO [Inventory_Stock] (variant_id, warehouse_id, quantity_on_hand)
SELECT v.variant_id, w.warehouse_id, 35
FROM [Product_Variants] v, [Warehouses] w
WHERE v.sku = 'OAK-HB-TORT' AND w.name = N'Kho chính Hà Nội'
AND NOT EXISTS (SELECT 1 FROM [Inventory_Stock] s WHERE s.variant_id = v.variant_id AND s.warehouse_id = w.warehouse_id);

INSERT INTO [Inventory_Stock] (variant_id, warehouse_id, quantity_on_hand)
SELECT v.variant_id, w.warehouse_id, 10
FROM [Product_Variants] v, [Warehouses] w
WHERE v.sku = 'GG-0061-BLACK' AND w.name = N'Kho chính Hà Nội'
AND NOT EXISTS (SELECT 1 FROM [Inventory_Stock] s WHERE s.variant_id = v.variant_id AND s.warehouse_id = w.warehouse_id);

-- HCM warehouse
INSERT INTO [Inventory_Stock] (variant_id, warehouse_id, quantity_on_hand)
SELECT v.variant_id, w.warehouse_id, 20
FROM [Product_Variants] v, [Warehouses] w
WHERE v.sku = 'RB-AV-GOLD' AND w.name = N'Kho TP.HCM'
AND NOT EXISTS (SELECT 1 FROM [Inventory_Stock] s WHERE s.variant_id = v.variant_id AND s.warehouse_id = w.warehouse_id);

INSERT INTO [Inventory_Stock] (variant_id, warehouse_id, quantity_on_hand)
SELECT v.variant_id, w.warehouse_id, 15
FROM [Product_Variants] v, [Warehouses] w
WHERE v.sku = 'OAK-HB-BLACK' AND w.name = N'Kho TP.HCM'
AND NOT EXISTS (SELECT 1 FROM [Inventory_Stock] s WHERE s.variant_id = v.variant_id AND s.warehouse_id = w.warehouse_id);

-- =============================================
-- 13. PROMOTIONS
-- =============================================
IF NOT EXISTS (SELECT * FROM [Promotions] WHERE code = 'WELCOME10')
    INSERT INTO [Promotions] (promotion_id, code, discount_type, value) VALUES (NEWID(), 'WELCOME10', 'PERCENTAGE', 10);

IF NOT EXISTS (SELECT * FROM [Promotions] WHERE code = 'NEWYEAR50K')
    INSERT INTO [Promotions] (promotion_id, code, discount_type, value) VALUES (NEWID(), 'NEWYEAR50K', 'FIXED', 50000);

IF NOT EXISTS (SELECT * FROM [Promotions] WHERE code = 'VIP20')
    INSERT INTO [Promotions] (promotion_id, code, discount_type, value) VALUES (NEWID(), 'VIP20', 'PERCENTAGE', 20);

-- =============================================
-- 14. SYSTEM_CONFIGS
-- =============================================
IF NOT EXISTS (SELECT * FROM [System_Configs] WHERE config_key = 'shop_name')
    INSERT INTO [System_Configs] (config_key, config_value, config_group) VALUES ('shop_name', N'CClearly - Kính Mắt Chính Hãng', 'general');

IF NOT EXISTS (SELECT * FROM [System_Configs] WHERE config_key = 'shop_email')
    INSERT INTO [System_Configs] (config_key, config_value, config_group) VALUES ('shop_email', 'contact@cclearly.com', 'general');

IF NOT EXISTS (SELECT * FROM [System_Configs] WHERE config_key = 'shop_phone')
    INSERT INTO [System_Configs] (config_key, config_value, config_group) VALUES ('shop_phone', '1900 1234', 'general');

IF NOT EXISTS (SELECT * FROM [System_Configs] WHERE config_key = 'shop_address')
    INSERT INTO [System_Configs] (config_key, config_value, config_group) VALUES ('shop_address', N'123 Nguyễn Huệ, Quận 1, TP.HCM', 'general');

IF NOT EXISTS (SELECT * FROM [System_Configs] WHERE config_key = 'currency')
    INSERT INTO [System_Configs] (config_key, config_value, config_group) VALUES ('currency', 'VND', 'payment');

IF NOT EXISTS (SELECT * FROM [System_Configs] WHERE config_key = 'tax_rate')
    INSERT INTO [System_Configs] (config_key, config_value, config_group) VALUES ('tax_rate', '10', 'payment');

IF NOT EXISTS (SELECT * FROM [System_Configs] WHERE config_key = 'free_shipping_threshold')
    INSERT INTO [System_Configs] (config_key, config_value, config_group) VALUES ('free_shipping_threshold', '500000', 'shipping');

IF NOT EXISTS (SELECT * FROM [System_Configs] WHERE config_key = 'default_shipping_fee')
    INSERT INTO [System_Configs] (config_key, config_value, config_group) VALUES ('default_shipping_fee', '30000', 'shipping');

IF NOT EXISTS (SELECT * FROM [System_Configs] WHERE config_key = 'max_cart_items')
    INSERT INTO [System_Configs] (config_key, config_value, config_group) VALUES ('max_cart_items', '20', 'cart');

IF NOT EXISTS (SELECT * FROM [System_Configs] WHERE config_key = 'order_expiry_minutes')
    INSERT INTO [System_Configs] (config_key, config_value, config_group) VALUES ('order_expiry_minutes', '30', 'order');

-- =============================================
-- 15. CONTENT_BANNERS
-- =============================================
IF NOT EXISTS (SELECT * FROM [Content_Banners] WHERE position = 'HOME_HERO')
    INSERT INTO [Content_Banners] (banner_id, image_url, position) VALUES (NEWID(), 'https://res.cloudinary.com/cclearly/image/upload/v1/banners/hero-main.jpg', 'HOME_HERO');

IF NOT EXISTS (SELECT * FROM [Content_Banners] WHERE position = 'HOME_PROMO')
    INSERT INTO [Content_Banners] (banner_id, image_url, position) VALUES (NEWID(), 'https://res.cloudinary.com/cclearly/image/upload/v1/banners/promo-summer.jpg', 'HOME_PROMO');

IF NOT EXISTS (SELECT * FROM [Content_Banners] WHERE position = 'HOME_NEW')
    INSERT INTO [Content_Banners] (banner_id, image_url, position) VALUES (NEWID(), 'https://res.cloudinary.com/cclearly/image/upload/v1/banners/new-arrivals.jpg', 'HOME_NEW');