package org.example.be_sp.controller;

import java.util.List;
import java.util.Map;

import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.model.request.CustomerLoginRequest;
import org.example.be_sp.model.request.CustomerRefreshTokenRequest;
import org.example.be_sp.model.response.CustomerLoginResponse;
import org.example.be_sp.model.response.CustomerProfileResponse;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.security.JwtUtils;
import org.example.be_sp.service.TokenBlacklistService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/khach-hang/auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseObject<CustomerLoginResponse> login(@RequestBody CustomerLoginRequest request) {
        if (request == null || request.getPassword() == null || request.getPassword().isBlank()) {
            return new ResponseObject<>(false, null, "Thiếu thông tin đăng nhập");
        }

        String identifier = request.resolveIdentifier();
        if (identifier == null || identifier.isBlank()) {
            return new ResponseObject<>(false, null, "Vui lòng nhập email hoặc tên tài khoản");
        }

        KhachHang khachHang = findCustomerByIdentifier(identifier);
        if (khachHang == null) {
            return new ResponseObject<>(false, null, "Tài khoản hoặc email không tồn tại");
        }

        if (!Boolean.TRUE.equals(khachHang.getTrangThai())) {
            return new ResponseObject<>(false, null, "Tài khoản đã bị khóa");
        }

        if (!passwordEncoder.matches(request.getPassword(), khachHang.getMatKhau())) {
            return new ResponseObject<>(false, null, "Mật khẩu không đúng");
        }

        String subject = khachHang.getTenTaiKhoan() != null && !khachHang.getTenTaiKhoan().isBlank()
                ? khachHang.getTenTaiKhoan()
                : khachHang.getEmail();

        List<String> roles = List.of("customer");
        String accessToken = jwtUtils.generateToken(subject, roles);
        String refreshToken = jwtUtils.generateRefreshToken(subject);

        CustomerProfileResponse profile = new CustomerProfileResponse(khachHang);

        CustomerLoginResponse response = new CustomerLoginResponse(accessToken, refreshToken, profile);
        return new ResponseObject<>(true, response, "Đăng nhập thành công");
    }

    @PostMapping("/refresh")
    public ResponseObject<?> refresh(@RequestBody CustomerRefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return new ResponseObject<>(false, null, "Refresh token không hợp lệ");
        }

        String refreshToken = request.getRefreshToken();

        try {
            if (jwtUtils.isTokenExpired(refreshToken)) {
                return new ResponseObject<>(false, null, "Refresh token đã hết hạn");
            }

            String subject = jwtUtils.getUsernameFromToken(refreshToken);
            KhachHang khachHang = findCustomerByIdentifier(subject);
            if (khachHang == null) {
                return new ResponseObject<>(false, null, "Không tìm thấy khách hàng");
            }

            String newAccessToken = jwtUtils.generateToken(subject, List.of("customer"));
            return new ResponseObject<>(true, Map.of("accessToken", newAccessToken), "Tạo access token mới thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Refresh token không hợp lệ");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseObject<CustomerProfileResponse>> me(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(new ResponseObject<>(false, null, "Thiếu token xác thực"));
        }

        String token = authHeader.substring(7);
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            return ResponseEntity.status(401).body(new ResponseObject<>(false, null, "Token không hợp lệ"));
        }

        try {
            if (jwtUtils.isTokenExpired(token)) {
                return ResponseEntity.status(401).body(new ResponseObject<>(false, null, "Token đã hết hạn"));
            }
            String subject = jwtUtils.getUsernameFromToken(token);
            KhachHang khachHang = findCustomerByIdentifier(subject);
            if (khachHang == null) {
                return ResponseEntity.status(404).body(new ResponseObject<>(false, null, "Không tìm thấy khách hàng"));
            }

            CustomerProfileResponse profile = new CustomerProfileResponse(khachHang);
            return ResponseEntity.ok(new ResponseObject<>(true, profile, "Thông tin khách hàng"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ResponseObject<>(false, null, "Token không hợp lệ"));
        }
    }

    private KhachHang findCustomerByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }
        KhachHang byUsername = khachHangRepository.findByTenTaiKhoan(identifier);
        if (byUsername != null) {
            return byUsername;
        }
        return khachHangRepository.findByEmail(identifier);
    }
}


