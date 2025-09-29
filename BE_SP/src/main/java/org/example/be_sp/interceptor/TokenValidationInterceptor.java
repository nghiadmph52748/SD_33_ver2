package org.example.be_sp.interceptor;

import java.lang.reflect.Method;

import org.example.be_sp.annotation.RequireAuth;
import org.example.be_sp.security.JwtUtils;
import org.example.be_sp.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;

/**
 * Interceptor để validate token trước khi gọi API
 */
@Component
public class TokenValidationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
            throws Exception {

        // Chỉ xử lý cho HandlerMethod (controller methods)
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Class<?> clazz = handlerMethod.getBeanType();

        // Kiểm tra annotation @RequireAuth trên method hoặc class
        RequireAuth requireAuth = method.getAnnotation(RequireAuth.class);
        if (requireAuth == null) {
            requireAuth = clazz.getAnnotation(RequireAuth.class);
        }

        // Nếu không có annotation @RequireAuth thì skip validation
        if (requireAuth == null) {
            return true;
        }

        // Token validation for protected endpoints

        // Lấy token từ header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(response, "Token không được cung cấp");
        }

        String token = authHeader.substring(7);

        try {
            // Kiểm tra token có trong blacklist không
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                return handleUnauthorized(response, "Token đã bị vô hiệu hóa");
            }

            // Lấy username và validate token
            String username = jwtUtils.getUsernameFromToken(token);
            if (username == null || username.trim().isEmpty()) {
                return handleUnauthorized(response, "Token không hợp lệ");
            }

            // Validate token
            if (!jwtUtils.validateToken(token, username)) {
                return handleUnauthorized(response, requireAuth.message());
            }

            // Kiểm tra token đã expired chưa
            if (jwtUtils.isTokenExpired(token)) {
                return handleUnauthorized(response, "Token đã hết hạn");
            }


            // Thêm username vào request attribute để controller có thể sử dụng
            request.setAttribute("currentUsername", username);
            request.setAttribute("validatedToken", token);

            return true;

        } catch (Exception e) {
            System.out.println("❌ Token validation error: " + e.getMessage());
            return handleUnauthorized(response, "Lỗi xác thực token: " + e.getMessage());
        }
    }

    /**
     * Xử lý khi token không hợp lệ
     */
    private boolean handleUnauthorized(HttpServletResponse response, String errorMessage) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // Tạo response object
        ObjectMapper mapper = new ObjectMapper();
        String jsonResponse = String.format("{\"success\":false,\"message\":%s,\"data\":null}", mapper.writeValueAsString(errorMessage));

        response.getWriter().write(jsonResponse);
        return false;
    }
}
