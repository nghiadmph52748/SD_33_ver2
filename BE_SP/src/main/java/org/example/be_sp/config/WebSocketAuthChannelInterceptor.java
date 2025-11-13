package org.example.be_sp.config;

import java.util.List;

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
    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    public WebSocketAuthChannelInterceptor(
            JwtUtils jwtUtils,
            UserDetailsService userDetailsService,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Trích xuất JWT token từ header trong quá trình CONNECT
            List<String> authHeaders = accessor.getNativeHeader("Authorization");

            if (authHeaders == null || authHeaders.isEmpty()) {
                throw buildUnauthorizedException("Thiếu header Authorization cho kết nối WebSocket");
            }

            String authHeader = authHeaders.get(0);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw buildUnauthorizedException("Định dạng Authorization header không hợp lệ");
            }

            String token = authHeader.substring(7);

            try {
                if (tokenBlacklistService.isTokenBlacklisted(token)) {
                    throw buildUnauthorizedException("Token đã bị thu hồi");
                }

                // Xác thực và trích xuất username từ token
                String username = jwtUtils.getUsernameFromToken(token);

                if (username == null || jwtUtils.isTokenExpired(token)) {
                    throw buildUnauthorizedException("Token hết hạn hoặc không hợp lệ");
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                List<SimpleGrantedAuthority> authorities = jwtUtils.getRolesFromToken(token)
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

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
                if (log.isDebugEnabled()) {
                    log.debug("[WebSocket Auth] Token validation failed: {}", e.getMessage());
                }
                throw buildUnauthorizedException("JWT token không hợp lệ", e);
            }
        }
        
        return message;
    }

    private IllegalArgumentException buildUnauthorizedException(String message) {
        return new IllegalArgumentException(message);
    }

    private IllegalArgumentException buildUnauthorizedException(String message, Exception cause) {
        return new IllegalArgumentException(message, cause);
    }
}
