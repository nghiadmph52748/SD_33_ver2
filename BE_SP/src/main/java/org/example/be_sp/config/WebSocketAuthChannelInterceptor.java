package org.example.be_sp.config;

import java.util.List;

import org.example.be_sp.security.JwtUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Interceptor cho kênh WebSocket để xác thực người dùng qua JWT token
 * Xác thực JWT token trong quá trình bắt tay WebSocket (lệnh CONNECT)
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;

    public WebSocketAuthChannelInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Trích xuất JWT token từ header trong quá trình CONNECT
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String authHeader = authHeaders.get(0);
                
                // Trích xuất token (format: "Bearer <token>")
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    
                    try {
                        // Xác thực và trích xuất username từ token
                        String username = jwtUtils.getUsernameFromToken(token);
                        
                        if (username != null && !jwtUtils.isTokenExpired(token)) {
                            // Tạo authentication và gán vào accessor
                            UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(username, null, List.of());
                            
                            System.out.println("✅ [WebSocket Auth] User authenticated: " + username);
                            System.out.println("✅ [WebSocket Auth] Principal name: " + authentication.getName());
                            
                            // Gán user vào STOMP session
                            accessor.setUser(authentication);
                            
                            // Cũng gán vào SecurityContext cho các phương thức @MessageMapping
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            
                            System.out.println("✅ [WebSocket Auth] Session user set to: " + accessor.getUser());
                        }
                    } catch (Exception e) {
                        // Token không hợp lệ - kết nối sẽ bị từ chối
                        throw new IllegalArgumentException("JWT token không hợp lệ", e);
                    }
                }
            }
        }
        
        return message;
    }
}
