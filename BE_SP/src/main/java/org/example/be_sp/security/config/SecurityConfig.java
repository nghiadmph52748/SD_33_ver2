package org.example.be_sp.security.config;

import java.util.List;

import org.example.be_sp.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Allow login and logout endpoints
                .requestMatchers("/api/khach-hang/auth/**").permitAll() // Allow storefront customer auth
                .requestMatchers("/api/ca-lam-viec/**").permitAll()
                .requestMatchers("/api/lich-lam-viec/**").permitAll()
                .requestMatchers("/api/giao-ca/**").permitAll()
                .requestMatchers("/api/content-data", "/api/popular/**").permitAll() // Allow dashboard mock data
                .requestMatchers("/api/payment/vnpay/**").permitAll() // Allow VNPAY redirect/IPN without auth
                .requestMatchers("/ws-chat/**").permitAll() // Allow WebSocket connection (auth handled by interceptor)
                .requestMatchers("/api/hoa-don-management/**").permitAll() // Allow invoice management without auth for testing
                .requestMatchers("/api/hoa-don-chi-tiet-management/**").permitAll() // Allow invoice detail management without auth for testing
                .requestMatchers("/api/thong-tin-hoa-don-management/**").permitAll() // Allow order info management without auth for testing
                .requestMatchers("/api/san-pham-management/**").permitAll() // Allow product management without auth for testing
                .requestMatchers("/api/chi-tiet-san-pham-management/**").permitAll() // Allow product detail management without auth for testing
                .requestMatchers("/api/khach-hang-management/**").permitAll() // Allow customer management without auth for testing
                .requestMatchers("/api/pos/**").permitAll() // Allow POS endpoints without auth for testing
                .requestMatchers("/api/test/**").permitAll() // Allow test endpoints without auth
                .requestMatchers("/api/email-test/**").permitAll() // Allow email test endpoints without auth
                .requestMatchers("/api/dot-giam-gia-management/**").permitAll() // Allow discount management without auth for testing
                .requestMatchers("/api/phieu-giam-gia-management/**").permitAll() // Allow coupon management without auth for testing
                .requestMatchers("/api/chi-tiet-phieu-giam-gia-management/**").permitAll() // Allow coupon detail management without auth for testing
                .requestMatchers("/api/anh-san-pham-management/**").permitAll() // Allow product images without auth for storefront
                .requestMatchers("/api/bien-the/**").permitAll() // Allow product variants without auth for testing
                .requestMatchers("/api/ai/**").permitAll()
                .requestMatchers("/api/anh-san-pham-management/**").permitAll() // Allow AI service endpoints without auth
                .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:5173", "http://localhost:5174")); // Add port 5174
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);  // Required for auth cookies/headers
        configuration.setExposedHeaders(List.of("*")); // Expose all headers
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
