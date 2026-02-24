package com.swp391.cclearly.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Value("${app.frontend-url:http://localhost:3000}")
  private String frontendUrl;

  /**
   * Send OTP verification email
   */
  @Async
  public void sendOtpEmail(String toEmail, String fullName, String otpCode) {
    String subject = "CClearly - Xác thực tài khoản";
    String content = buildOtpEmailContent(fullName, otpCode);
    sendEmail(toEmail, subject, content);
  }

  /**
   * Send password reset email
   */
  @Async
  public void sendPasswordResetEmail(String toEmail, String fullName, String resetToken) {
    String subject = "CClearly - Đặt lại mật khẩu";
    String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
    String content = buildResetPasswordEmailContent(fullName, resetLink);
    sendEmail(toEmail, subject, content);
  }

  /**
   * Send welcome email after registration
   */
  @Async
  public void sendWelcomeEmail(String toEmail, String fullName) {
    String subject = "Chào mừng bạn đến với CClearly!";
    String content = buildWelcomeEmailContent(fullName);
    sendEmail(toEmail, subject, content);
  }

  private void sendEmail(String toEmail, String subject, String htmlContent) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Email sent successfully to: {}", toEmail);
    } catch (MessagingException e) {
      log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
    }
  }

  private String buildOtpEmailContent(String fullName, String otpCode) {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background-color: #f9f9f9; }
                .otp-code { font-size: 32px; font-weight: bold; color: #4F46E5; text-align: center; padding: 20px; background-color: #fff; border-radius: 8px; margin: 20px 0; letter-spacing: 5px; }
                .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>CClearly</h1>
                </div>
                <div class="content">
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Cảm ơn bạn đã đăng ký tài khoản tại CClearly. Vui lòng sử dụng mã OTP bên dưới để xác thực email của bạn:</p>
                    <div class="otp-code">%s</div>
                    <p>Mã OTP này có hiệu lực trong <strong>5 phút</strong>.</p>
                    <p>Nếu bạn không yêu cầu đăng ký tài khoản, vui lòng bỏ qua email này.</p>
                </div>
                <div class="footer">
                    <p>© 2024 CClearly. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(fullName, otpCode);
  }

  private String buildResetPasswordEmailContent(String fullName, String resetLink) {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background-color: #f9f9f9; }
                .button { display: inline-block; background-color: #4F46E5; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>CClearly</h1>
                </div>
                <div class="content">
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Nhấn vào nút bên dưới để đặt lại mật khẩu:</p>
                    <p style="text-align: center;">
                        <a href="%s" class="button" style="color: white;">Đặt lại mật khẩu</a>
                    </p>
                    <p>Link này có hiệu lực trong <strong>30 phút</strong>.</p>
                    <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
                </div>
                <div class="footer">
                    <p>© 2024 CClearly. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(fullName, resetLink);
  }

  private String buildWelcomeEmailContent(String fullName) {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background-color: #f9f9f9; }
                .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>CClearly</h1>
                </div>
                <div class="content">
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Chào mừng bạn đến với CClearly - nơi cung cấp các sản phẩm kính mắt chất lượng cao!</p>
                    <p>Tài khoản của bạn đã được xác thực thành công. Bạn có thể bắt đầu khám phá các sản phẩm của chúng tôi.</p>
                </div>
                <div class="footer">
                    <p>© 2024 CClearly. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(fullName);
  }
}
