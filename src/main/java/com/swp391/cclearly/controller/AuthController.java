package com.swp391.cclearly.controller;

import com.swp391.cclearly.dto.auth.*;
import com.swp391.cclearly.dto.base.ApiResponse;
import com.swp391.cclearly.service.AuthService;
import com.swp391.cclearly.service.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs xác thực người dùng: đăng ký, đăng nhập, quên mật khẩu")
public class AuthController {

  private final AuthService authService;
  private final OAuth2Service oAuth2Service;

  @Operation(
      summary = "Đăng ký tài khoản mới",
      description = "Tạo tài khoản mới với email, mật khẩu và họ tên. Sau khi đăng ký, hệ thống sẽ gửi OTP qua email để xác thực."
  )
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Đăng ký thành công, OTP đã được gửi qua email",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "Dữ liệu không hợp lệ hoặc email đã tồn tại",
          content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }

  @Operation(
      summary = "Đăng nhập",
      description = "Đăng nhập bằng email và mật khẩu. Trả về JWT token để sử dụng cho các API khác."
  )
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Đăng nhập thành công",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "Email hoặc mật khẩu không đúng",
          content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @Operation(
      summary = "Đăng nhập bằng Google",
      description = "Đăng nhập hoặc đăng ký tự động bằng tài khoản Google. Gửi ID Token từ Google để xác thực."
  )
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Đăng nhập Google thành công",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "Token Google không hợp lệ",
          content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping("/google")
  public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(
      @Valid @RequestBody GoogleLoginRequest request) {
    return ResponseEntity.ok(oAuth2Service.googleLogin(request));
  }

  @Operation(
      summary = "Xác thực email",
      description = "Xác thực email bằng mã OTP đã được gửi qua email khi đăng ký."
  )
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Xác thực email thành công",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "OTP không đúng hoặc đã hết hạn",
          content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping("/verify-email")
  public ResponseEntity<ApiResponse<Void>> verifyEmail(
      @Valid @RequestBody VerifyEmailRequest request) {
    return ResponseEntity.ok(authService.verifyEmail(request));
  }

  @Operation(
      summary = "Gửi lại OTP",
      description = "Gửi lại mã OTP mới qua email để xác thực tài khoản."
  )
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "OTP mới đã được gửi qua email",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "Email không tồn tại trong hệ thống",
          content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping("/resend-otp")
  public ResponseEntity<ApiResponse<Void>> resendOtp(
      @Valid @RequestBody ResendOtpRequest request) {
    return ResponseEntity.ok(authService.resendOtp(request));
  }

  @Operation(
      summary = "Quên mật khẩu",
      description = "Yêu cầu đặt lại mật khẩu. Hệ thống sẽ gửi link reset password qua email."
  )
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Email reset password đã được gửi",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "Email không tồn tại trong hệ thống",
          content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse<Void>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    return ResponseEntity.ok(authService.forgotPassword(request));
  }

  @Operation(
      summary = "Đặt lại mật khẩu",
      description = "Đặt lại mật khẩu mới bằng token đã nhận qua email."
  )
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Đặt lại mật khẩu thành công",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "Token không hợp lệ hoặc đã hết hạn",
          content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    return ResponseEntity.ok(authService.resetPassword(request));
  }
}
