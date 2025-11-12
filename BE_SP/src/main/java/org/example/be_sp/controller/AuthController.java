package org.example.be_sp.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.model.request.LoginRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.security.JwtUtils;
import org.example.be_sp.service.NhanVienService;
import org.example.be_sp.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private NhanVienService nhanVienService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

//    @PostMapping("/verify-token")
//    public ResponseObject<?> verifyToken(@RequestParam String token) {
//        try {
//            String username = jwtUtils.getUsernameFromToken(token);
//            boolean isValid = jwtUtils.validateToken(token, username);
//            boolean isExpired = jwtUtils.isTokenExpired(token);
//
//            return new ResponseObject<>(true, new Object() {
//                public final String extractedUsername = username;
//                public final boolean valid = isValid;
//                public final boolean expired = isExpired;
//            }, "Token verification result");
//
//        } catch (Exception e) {
//            return new ResponseObject<>(false, null, "Token verification failed: " + e.getMessage());
//        }
//    }

    @RequestMapping(value = "/login", method = RequestMethod.OPTIONS)
    public ResponseObject<?> handleOptions() {
        return new ResponseObject<>(true, null, "OK");
    }

    @PostMapping("/login")
    public ResponseObject<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Tìm nhân viên
            NhanVien nhanVien = nhanVienService.findByTenTaiKhoan(loginRequest.getUsername());

            if (nhanVien == null) {
                return new ResponseObject<>(false, null, "Tên tài khoản không tồn tại");
            }

            if (!passwordEncoder.matches(loginRequest.getPassword(), nhanVien.getMatKhau())) {
                return new ResponseObject<>(false, null, "Mật khẩu không đúng");
            }

            if (!Boolean.TRUE.equals(nhanVien.getTrangThai())) {
                return new ResponseObject<>(false, null, "Tài khoản đã bị khóa");
            }

            // ✅ Lấy danh sách quyền của nhân viên (chuyển sang list string)
            // Nếu chỉ có 1 quyền, vẫn để thành list 1 phần tử
            // ✅ Lấy danh sách quyền của nhân viên
            // ✅ Lấy danh sách quyền của nhân viên và chuẩn hoá tên
            List<String> roles;

            if (nhanVien.getIdQuyenHan() != null) {
                Integer quyenId = nhanVien.getIdQuyenHan().getId();
                String normalizedRole;

                if (quyenId == 1) {
                    normalizedRole = "admin";
                } else if (quyenId == 2) {
                    normalizedRole = "nhan_vien";
                } else {
                    normalizedRole = "user";
                }

                roles = List.of(normalizedRole);
            } else {
                roles = List.of("user");
            }



            // ✅ Sinh tokens
            String accessToken = jwtUtils.generateToken(nhanVien.getTenTaiKhoan(), roles);
            String refreshToken = jwtUtils.generateRefreshToken(nhanVien.getTenTaiKhoan());

            // ✅ Chuẩn bị dữ liệu trả về
            LoginResponseData responseData = new LoginResponseData();
            responseData.setId(nhanVien.getId());
            responseData.setMaNhanVien(nhanVien.getMaNhanVien());
            responseData.setTenNhanVien(nhanVien.getTenNhanVien());
            responseData.setTenTaiKhoan(nhanVien.getTenTaiKhoan());
            responseData.setEmail(nhanVien.getEmail());
            responseData.setIdQuyenHan(nhanVien.getIdQuyenHan().getId());
            responseData.setTenQuyenHan(nhanVien.getIdQuyenHan().getTenQuyenHan());
            responseData.setAccessToken(accessToken);
            responseData.setRefreshToken(refreshToken);

            return new ResponseObject<>(true, responseData, "Đăng nhập thành công");

        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi hệ thống: " + e.getMessage());
        }
    }


    // Inner class cho response data
    public static class LoginResponseData {

        private Integer id;
        private String maNhanVien;
        private String tenNhanVien;
        private String tenTaiKhoan;
        private String email;
        private Integer idQuyenHan;
        private String tenQuyenHan;
        private String accessToken;
        private String refreshToken;

        // Getters and setters
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getMaNhanVien() {
            return maNhanVien;
        }

        public void setMaNhanVien(String maNhanVien) {
            this.maNhanVien = maNhanVien;
        }

        public String getTenNhanVien() {
            return tenNhanVien;
        }

        public void setTenNhanVien(String tenNhanVien) {
            this.tenNhanVien = tenNhanVien;
        }

        public String getTenTaiKhoan() {
            return tenTaiKhoan;
        }

        public void setTenTaiKhoan(String tenTaiKhoan) {
            this.tenTaiKhoan = tenTaiKhoan;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Integer getIdQuyenHan() {
            return idQuyenHan;
        }

        public void setIdQuyenHan(Integer idQuyenHan) {
            this.idQuyenHan = idQuyenHan;
        }

        public String getTenQuyenHan() {
            return tenQuyenHan;
        }

        public void setTenQuyenHan(String tenQuyenHan) {
            this.tenQuyenHan = tenQuyenHan;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    @PostMapping("/logout")
    public ResponseObject<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Extract and blacklist the current token if provided
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtils.getUsernameFromToken(token);
                Date expiryDate = jwtUtils.getExpirationDateFromToken(token);
                LocalDateTime expiryLocalDateTime = expiryDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                // Add token to blacklist
                tokenBlacklistService.addToBlacklist(token, username, expiryLocalDateTime);
            }

            return new ResponseObject<>(true, null, "Logged out successfully - token invalidated server-side");
        } catch (Exception e) {
            // Even if there's an error, we should still return success for logout
            return new ResponseObject<>(true, null, "Logged out successfully");
        }
    }
    @PostMapping("/forgot-password")
    public ResponseObject<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        try {
            nhanVienService.forgotPassword(email);
            return new ResponseObject<>(true, null, "Đã gửi email đặt lại mật khẩu!");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, e.getMessage());
        }
    }


    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        Map<String, Object> data = nhanVienService.verifyToken(token);
        boolean valid = (boolean) data.get("valid");
        boolean expired = (boolean) data.get("expired");
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of("valid", valid, "expired", expired)));
    }



    @PostMapping("/reset-password")
    public ResponseObject<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        try {
            nhanVienService.resetPassword(token, newPassword, passwordEncoder);
            return new ResponseObject<>(true, null, "Đặt lại mật khẩu thành công!");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, e.getMessage());
        }
    }


}
