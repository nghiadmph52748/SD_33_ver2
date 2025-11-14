package org.example.be_sp.config;

import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.security.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket cho tin nhắn chat thời gian thực
 * Sử dụng giao thức STOMP qua WebSocket
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtils jwtUtils;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    private final org.example.be_sp.service.TokenBlacklistService tokenBlacklistService;
    private final KhachHangRepository khachHangRepository;

    public WebSocketConfig(
            JwtUtils jwtUtils,
            org.springframework.security.core.userdetails.UserDetailsService userDetailsService,
            org.example.be_sp.service.TokenBlacklistService tokenBlacklistService,
            KhachHangRepository khachHangRepository
    ) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.khachHangRepository = khachHangRepository;
    }

    @Bean
    public WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor() {
        return new WebSocketAuthChannelInterceptor(jwtUtils, userDetailsService, tokenBlacklistService, khachHangRepository);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Bật message broker đơn giản trong bộ nhớ
        // Tin nhắn với tiền tố "/topic" sẽ được phát tới tất cả người đăng ký
        // Tin nhắn với tiền tố "/queue" sẽ được gửi đến từng người dùng riêng biệt
        config.enableSimpleBroker("/topic", "/queue");
        
        // Tiền tố cho các tin nhắn được xử lý bởi các phương thức @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // Tiền tố cho các đích đến cụ thể của người dùng
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký STOMP endpoint mà client sẽ kết nối đến
        // Endpoint: ws://localhost:8080/ws-chat
        registry.addEndpoint("/ws-chat")
                .setAllowedOrigins(
                    "http://localhost:5173", 
                    "http://localhost:5174",
                    "http://localhost:8080"
                )
                .withSockJS(); // Bật SockJS fallback cho các trình duyệt không hỗ trợ WebSocket
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Đăng ký interceptor xác thực JWT
        registration.interceptors(webSocketAuthChannelInterceptor());
    }
}
