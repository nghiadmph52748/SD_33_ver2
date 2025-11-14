package org.example.be_sp.config;

import java.util.List;

import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.security.JwtUtils;
import org.example.be_sp.service.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Interceptor cho kênh WebSocket để xác thực người dùng qua JWT token
 * Xác thực JWT token trong quá trình bắt tay WebSocket (lệnh CONNECT)
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final KhachHangRepository khachHangRepository;
    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    public WebSocketAuthChannelInterceptor(
            JwtUtils jwtUtils,
            UserDetailsService userDetailsService,
            TokenBlacklistService tokenBlacklistService,
            KhachHangRepository khachHangRepository
    ) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.khachHangRepository = khachHangRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            return message;
        }
        
        StompCommand command = accessor.getCommand();
        
        // Xử lý CONNECT command - xác thực JWT token
        if (StompCommand.CONNECT.equals(command)) {
            // Trích xuất JWT token từ header trong quá trình CONNECT
            List<String> authHeaders = accessor.getNativeHeader("Authorization");

            if (authHeaders == null || authHeaders.isEmpty()) {
                log.error("[WebSocket Auth] Missing Authorization header for CONNECT");
                throw buildUnauthorizedException("Thiếu header Authorization cho kết nối WebSocket");
            }

            String authHeader = authHeaders.get(0);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("[WebSocket Auth] Invalid Authorization header format");
                throw buildUnauthorizedException("Định dạng Authorization header không hợp lệ");
            }

            String token = authHeader.substring(7);

            try {
                if (tokenBlacklistService.isTokenBlacklisted(token)) {
                    log.error("[WebSocket Auth] Token is blacklisted");
                    throw buildUnauthorizedException("Token đã bị thu hồi");
                }

                // Xác thực và trích xuất username từ token
                String username = jwtUtils.getUsernameFromToken(token);

                if (username == null || jwtUtils.isTokenExpired(token)) {
                    log.error("[WebSocket Auth] Token expired or invalid");
                    throw buildUnauthorizedException("Token hết hạn hoặc không hợp lệ");
                }

                // Lấy roles từ token trước
                List<String> roles = jwtUtils.getRolesFromToken(token);
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // Kiểm tra xem có phải customer không
                boolean isCustomer = roles.stream()
                        .anyMatch(role -> role.equalsIgnoreCase("ROLE_CUSTOMER"));

                UserDetails userDetails;
                if (isCustomer) {
                    // Load customer từ KhachHangRepository
                    KhachHang khachHang = khachHangRepository.findByTenTaiKhoan(username);
                    if (khachHang == null) {
                        // Fallback: thử tìm theo email
                        khachHang = khachHangRepository.findByEmail(username);
                    }
                    if (khachHang == null) {
                        log.error("[WebSocket Auth] Customer not found: {}", username);
                        throw buildUnauthorizedException("Không tìm thấy khách hàng: " + username);
                    }
                    // Tạo UserDetails cho customer
                    userDetails = User.builder()
                            .username(khachHang.getTenTaiKhoan() != null ? khachHang.getTenTaiKhoan() : khachHang.getEmail())
                            .password(khachHang.getMatKhau() != null ? khachHang.getMatKhau() : "")
                            .disabled(!Boolean.TRUE.equals(khachHang.getTrangThai()))
                            .authorities(authorities)
                            .build();
                } else {
                    // Load staff từ UserDetailsService
                    userDetails = userDetailsService.loadUserByUsername(username);
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                if (log.isDebugEnabled()) {
                    log.debug("[WebSocket Auth] User authenticated: {}", username);
                    log.debug("[WebSocket Auth] Authorities: {}", authorities);
                }

                accessor.setUser(authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                if (log.isDebugEnabled()) {
                    log.debug("[WebSocket Auth] Session user set to: {}", accessor.getUser());
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                log.error("[WebSocket Auth] Token validation failed: {}", e.getMessage(), e);
                throw buildUnauthorizedException("JWT token không hợp lệ", e);
            }
        } 
        // Đối với các command khác (SEND, SUBSCRIBE, etc.), đảm bảo Principal vẫn còn
        else if (command != null && command != StompCommand.CONNECTED && command != StompCommand.DISCONNECT) {
            // Kiểm tra xem Principal có tồn tại không
            if (accessor.getUser() == null) {
                log.warn("[WebSocket Auth] No Principal found for command: {}", command);
                // Không throw exception ở đây để tránh block message, nhưng log warning
            } else {
                // Đảm bảo SecurityContext được set lại từ Principal
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken auth = 
                        (UsernamePasswordAuthenticationToken) accessor.getUser();
                    if (auth != null) {
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        if (log.isDebugEnabled()) {
                            log.debug("[WebSocket Auth] Restored SecurityContext for command: {}", command);
                        }
                    }
                }
            }
        }
        
        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        if (ex != null) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (accessor != null) {
                log.error("[WebSocket Auth] Error sending message for command: {}, error: {}", 
                    accessor.getCommand(), ex.getMessage(), ex);
            } else {
                log.error("[WebSocket Auth] Error sending message: {}", ex.getMessage(), ex);
            }
        }
    }

    private IllegalArgumentException buildUnauthorizedException(String message) {
        return new IllegalArgumentException(message);
    }

    private IllegalArgumentException buildUnauthorizedException(String message, Exception cause) {
        return new IllegalArgumentException(message, cause);
    }
}
