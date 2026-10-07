package com.example.TuVanSinhVien.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.allowed-origins:http://localhost:8080,http://127.0.0.1:8080}")
    private String[] allowedOrigins;

    private final RoleBasedAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;
    private final CaptchaAuthenticationFilter captchaFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final com.example.TuVanSinhVien.service.TokenRevocationService tokenRevocation;

    public SecurityConfig(RoleBasedAuthenticationSuccessHandler successHandler,
                          CustomAuthenticationFailureHandler failureHandler,
                          CaptchaAuthenticationFilter captchaFilter,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          com.example.TuVanSinhVien.service.TokenRevocationService tokenRevocation) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.captchaFilter = captchaFilter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tokenRevocation = tokenRevocation;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain bearerFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(request -> request.getHeader("Authorization") != null
                    || request.getServletPath().startsWith("/api/auth/"))
            .csrf(csrf -> csrf.disable()) // This chain never authenticates from cookies.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
            .securityContext(context -> context.securityContextRepository(
                    new org.springframework.security.web.context.NullSecurityContextRepository()))
            .requestCache(cache -> cache.disable())
            .logout(logout -> logout.disable())
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.INCLUDE, DispatcherType.ERROR).permitAll()
                .requestMatchers("/WEB-INF/**", "/advisor-register", "/advisor-register/**").denyAll()
                .requestMatchers("/api/auth/token", "/api/public/**").permitAll()
                .requestMatchers("/student/**").hasRole("SinhVien")
                .requestMatchers("/advisor/**").hasRole("CoVan")
                .requestMatchers("/admin/**").hasRole("Admin")
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, error) -> writeApiError(response, 401,
                        "Phiên đăng nhập đã hết hạn hoặc token không hợp lệ. Vui lòng đăng nhập lại."))
                .accessDeniedHandler((request, response, error) -> writeApiError(response, 403,
                        "Bạn không có quyền thực hiện thao tác này.")));
        return http.build();
    }

    private static void writeApiError(jakarta.servlet.http.HttpServletResponse response, int status, String message)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    @Bean
    @org.springframework.core.annotation.Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();
        csrfRequestHandler.setCsrfRequestAttributeName(null);

        http
            // 1. Chống CSRF (chỉ miễn trừ WebSocket & REST API nếu dùng token)
            .csrf(csrf -> csrf
                .csrfTokenRequestHandler(csrfRequestHandler)
                .ignoringRequestMatchers("/ws/**", "/api/auth/token")
            )
            // 2. CORS Allowlist
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 3. Gia cố HTTP Security Headers & Content Security Policy (CSP)
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' https:; style-src 'self' 'unsafe-inline' https:; font-src 'self' data: https:; img-src 'self' data: https: blob:; connect-src 'self' ws: wss:; frame-ancestors 'none'; object-src 'none';")
                )
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .permissionsPolicy(permissions -> permissions
                    .policy("geolocation=(), microphone=(), camera=()")
                )
            )
            // 4. Bảo mật Phiên làm việc (Session Security)
            .sessionManagement(session -> session
                .sessionFixation(fixation -> fixation.migrateSession())
                .maximumSessions(2)
                .expiredUrl("/login?expired=true")
            )
            // 5. Phân quyền Request
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.INCLUDE, DispatcherType.ERROR).permitAll()
                .requestMatchers("/WEB-INF/**", "/advisor-register", "/advisor-register/**").denyAll()
                .requestMatchers("/ws/**").authenticated()
                .requestMatchers(
                    "/",
                    "/faq/**",
                    "/forum/**",
                    "/articles/**",
                    "/attachments/**",
                    "/advisors/**",
                    "/login",
                    "/register",
                    "/verify-otp",
                    "/resend-otp",
                    "/captcha",
                    "/forgot-password",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/api/public/**",
                    "/api/auth/token"
                ).permitAll()
                .requestMatchers("/student/**").hasRole("SinhVien")
                .requestMatchers("/advisor/**").hasRole("CoVan")
                .requestMatchers("/admin/**").hasRole("Admin")
                .anyRequest().authenticated()
            )
            // 6. Bộ lọc kiểm tra Brute-force & CAPTCHA trước UsernamePasswordAuthenticationFilter
            .addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, CaptchaAuthenticationFilter.class)
            // 7. Form Login với Custom Failure Handler
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            // 8. Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler((request, response, authentication) -> {
                    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails user) {
                        tokenRevocation.revokeAll(user.getMaTK());
                    }
                })
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // 9. Xử lý từ chối truy cập thông minh (Redirect theo role thay vì lỗi 403)
            .exceptionHandling(exceptions -> exceptions
                .defaultAuthenticationEntryPointFor(
                    new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/login"),
                    request -> !request.getServletPath().startsWith("/api/") && !request.getServletPath().startsWith("/ws/"))
                .defaultAuthenticationEntryPointFor((request, response, exception) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.\"}");
                }, request -> request.getServletPath().startsWith("/api/") || request.getServletPath().startsWith("/ws/"))
                .accessDeniedHandler((request, response, exception) -> {
                    if (request.getServletPath().startsWith("/api/")) {
                        response.setStatus(403);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\":\"Bạn không có quyền truy cập phiên chat này.\"}");
                    } else {
                        accessDeniedHandler.handle(request, response, exception);
                    }
                })
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<CaptchaAuthenticationFilter> captchaFilterRegistration(CaptchaAuthenticationFilter filter) {
        org.springframework.boot.web.servlet.FilterRegistrationBean<CaptchaAuthenticationFilter> registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        var registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
