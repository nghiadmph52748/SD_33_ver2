package org.example.be_sp.security;

import java.io.IOException;
import java.util.List;

import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String path = request.getServletPath();

        // Bỏ qua filter cho các endpoint public (không yêu cầu đăng nhập)
        if (path.startsWith("/api/auth/")
                || path.startsWith("/api/khach-hang/auth/")
                || path.startsWith("/api/khach-hang/me")
                || path.startsWith("/api/user/reset-password")
                || path.startsWith("/api/email-test/")
                || path.startsWith("/api/test/")
                || path.startsWith("/api/payment/vnpay/")
                || path.startsWith("/api/chi-tiet-san-pham-management/")
                || path.startsWith("/api/anh-san-pham-management/")
                || path.startsWith("/api/nhan-vien-management/") // employee management is temporarily public
        ) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Bỏ qua nếu không có header hoặc không bắt đầu bằng "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            username = jwtUtils.getUsernameFromToken(jwt);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or malformed token");
            return;
        }

        // Kiểm tra token có bị thu hồi không
        if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has been invalidated");
            return;
        }

        // Nếu chưa có authentication trong context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Lấy roles từ token trước
                List<String> roles = jwtUtils.getRolesFromToken(jwt);

                // Kiểm tra xem có phải customer không
                boolean isCustomer = roles.stream()
                        .anyMatch(role -> role.equalsIgnoreCase("ROLE_CUSTOMER"));

                UserDetails userDetails;
                String usernameForValidation;

                if (isCustomer) {
                    // Load customer từ KhachHangRepository
                    KhachHang khachHang = khachHangRepository.findByTenTaiKhoan(username);
                    if (khachHang == null) {
                        // Fallback: thử tìm theo email
                        khachHang = khachHangRepository.findByEmail(username);
                    }
                    if (khachHang == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Customer not found");
                        return;
                    }
                    // Tạo UserDetails cho customer
                    usernameForValidation = khachHang.getTenTaiKhoan() != null ? khachHang.getTenTaiKhoan()
                            : khachHang.getEmail();
                    userDetails = User.builder()
                            .username(usernameForValidation)
                            .password(khachHang.getMatKhau() != null ? khachHang.getMatKhau() : "")
                            .disabled(!Boolean.TRUE.equals(khachHang.getTrangThai()))
                            .authorities(roles.stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .toList())
                            .build();
                } else {
                    // Load staff từ UserDetailsService
                    userDetails = userDetailsService.loadUserByUsername(username);
                    usernameForValidation = userDetails.getUsername();
                }

                // Validate và refresh token nếu gần hết hạn
                String refreshedToken = jwtUtils.validateAndRefreshToken(jwt, usernameForValidation);

                if (refreshedToken != null) {
                    // Nếu token được refresh, gửi token mới về header
                    if (!refreshedToken.equals(jwt)) {
                        response.setHeader("New-Token", refreshedToken);
                    }

                    // Gán quyền cho user từ token
                    var authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities);

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid or expired token");
                    return;
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token validation failed");
                return;
            }
        }

        // Cho phép request đi tiếp nếu hợp lệ
        filterChain.doFilter(request, response);
    }
}
