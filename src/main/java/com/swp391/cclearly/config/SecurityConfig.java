package com.swp391.cclearly.config;

import com.swp391.cclearly.exception.CustomAccessDeniedHandler;
import com.swp391.cclearly.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(request -> {
          var corsConfig = new org.springframework.web.cors.CorsConfiguration();
          corsConfig.addAllowedOriginPattern("*");
          corsConfig.addAllowedMethod("*");
          corsConfig.addAllowedHeader("*");
          corsConfig.setAllowCredentials(true);
          return corsConfig;
        }))
        .authorizeHttpRequests(auth -> auth
            // ✅ Public endpoints - Auth APIs
            .requestMatchers(
                "/api/v1/auth/**",
                "/api/v1/public/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/v3/api-docs",
                "/webjars/**",
                "/actuator/**"
            ).permitAll()

            // ✅ Public GET endpoints - Products
            .requestMatchers(HttpMethod.GET,
                "/api/v1/products/**",
                "/api/v1/frames/**",
                "/api/v1/lenses/**",
                "/api/v1/accessories/**",
                "/api/v1/categories/**"
            ).permitAll()

            // 🔒 Admin only endpoints
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

            // 🔒 Manager endpoints
            .requestMatchers("/api/v1/manager/**").hasAnyRole("ADMIN", "MANAGER")

            // 🔒 Sales Staff endpoints
            .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyRole("ADMIN", "MANAGER", "SALES_STAFF")
            .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAnyRole("ADMIN", "MANAGER", "SALES_STAFF")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAnyRole("ADMIN", "MANAGER")

            // 🔒 Operation Staff endpoints
            .requestMatchers("/api/v1/inventory/**").hasAnyRole("ADMIN", "MANAGER", "OPERATION_STAFF")
            .requestMatchers("/api/v1/shipping/**").hasAnyRole("ADMIN", "MANAGER", "OPERATION_STAFF")

            // 🔒 Customer endpoints
            .requestMatchers("/api/v1/user/**").hasAnyRole("CUSTOMER", "ADMIN")
            .requestMatchers("/api/v1/orders/**").authenticated()
            .requestMatchers("/api/v1/cart/**").authenticated()

            // 🔒 All other requests require authentication
            .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
            .accessDeniedHandler(customAccessDeniedHandler)
            .authenticationEntryPoint(customAuthenticationEntryPoint)
        )
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
