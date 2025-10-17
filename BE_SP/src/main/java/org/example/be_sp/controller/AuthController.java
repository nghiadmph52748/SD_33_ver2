package org.example.be_sp.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.model.request.LoginRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.security.JwtUtils;
import org.example.be_sp.service.NhanVienService;
import org.example.be_sp.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/verify-token")
    public ResponseObject<?> verifyToken(@RequestParam String token) {
        try {
            String username = jwtUtils.getUsernameFromToken(token);
            boolean isValid = jwtUtils.validateToken(token, username);
            boolean isExpired = jwtUtils.isTokenExpired(token);

            return new ResponseObject<>(true, new Object() {
                public final String extractedUsername = username;
                public final boolean valid = isValid;
                public final boolean expired = isExpired;
            }, "Token verification result");

        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Token verification failed: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.OPTIONS)
    public ResponseObject<?> handleOptions() {
        return new ResponseObject<>(true, null, "OK");
    }

    @PostMapping("/login")
    public ResponseObject<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Hardcoded admin exception for development/testing
            if (("admin".equals(loginRequest.getUsername()) || "admin@gearup.com".equals(loginRequest.getUsername())) 
                && "admin".equals(loginRequest.getPassword())) {
                String accessToken = jwtUtils.generateToken("admin");
                String refreshToken = jwtUtils.generateRefreshToken("admin");
                
                LoginResponseData adminData = new LoginResponseData();
                adminData.setId(1); // Default admin ID
                adminData.setMaNhanVien("ADMIN001");
                adminData.setTenNhanVien("Administrator");
                adminData.setTenTaiKhoan("admin");
                adminData.setEmail("admin@gearup.com");
                adminData.setIdQuyenHan(1); // Admin role ID
                adminData.setTenQuyenHan("Admin");
                adminData.setAccessToken(accessToken);
                adminData.setRefreshToken(refreshToken);
                
                return new ResponseObject<>(true, adminData, "Đăng nhập thành công (Admin)");
            }
            
            // Tìm nhân viên theo tên tài khoản hoặc email
            String identifier = loginRequest.getUsername();
            NhanVien nhanVien = null;
            
            // Kiểm tra xem identifier có phải email không (chứa @)
            if (identifier.contains("@")) {
                nhanVien = nhanVienService.findByEmail(identifier);
            } else {
                nhanVien = nhanVienService.findByTenTaiKhoan(identifier);
            }

            if (nhanVien == null) {
                return new ResponseObject<>(false, null, "Tên tài khoản hoặc email không tồn tại");
            }

            // Kiểm tra mật khẩu - hỗ trợ cả plain text và BCrypt
            String storedPassword = nhanVien.getMatKhau();
            String inputPassword = loginRequest.getPassword();
            
            boolean passwordMatches = false;
            
            // Kiểm tra xem mật khẩu trong DB có phải BCrypt không
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                // Mật khẩu đã được mã hóa BCrypt
                passwordMatches = passwordEncoder.matches(inputPassword, storedPassword);
            } else {
                // Mật khẩu plain text (cho môi trường dev/test)
                passwordMatches = inputPassword.equals(storedPassword);
            }
            
            if (!passwordMatches) {
                return new ResponseObject<>(false, null, "Mật khẩu không đúng");
            }

            // Kiểm tra trạng thái
            if (!Boolean.TRUE.equals(nhanVien.getTrangThai())) {
                return new ResponseObject<>(false, null, "Tài khoản đã bị khóa");
            }

            // Tạo JWT tokens
            String accessToken = jwtUtils.generateToken(nhanVien.getTenTaiKhoan());
            String refreshToken = jwtUtils.generateRefreshToken(nhanVien.getTenTaiKhoan());

            // Tạo response data
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

    @PostMapping("/me")
    public ResponseObject<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new ResponseObject<>(false, null, "No token provided");
            }

            String token = authHeader.substring(7);
            String username = jwtUtils.getUsernameFromToken(token);

            if (!jwtUtils.validateToken(token, username)) {
                return new ResponseObject<>(false, null, "Invalid token");
            }

            // Hardcoded admin bypass for /me endpoint
            if ("admin".equals(username)) {
                LoginResponseData adminData = new LoginResponseData();
                adminData.setId(1);
                adminData.setMaNhanVien("ADMIN001");
                adminData.setTenNhanVien("Administrator");
                adminData.setTenTaiKhoan("admin");
                adminData.setEmail("admin@gearup.com");
                adminData.setIdQuyenHan(1);
                adminData.setTenQuyenHan("Admin");
                adminData.setAccessToken(token);
                adminData.setRefreshToken(null);
                
                return new ResponseObject<>(true, adminData, "User info retrieved successfully (Admin)");
            }

            // Tìm nhân viên theo tên tài khoản
            NhanVien nhanVien = nhanVienService.findByTenTaiKhoan(username);

            if (nhanVien == null) {
                return new ResponseObject<>(false, null, "User not found");
            }

            // Kiểm tra trạng thái
            if (!Boolean.TRUE.equals(nhanVien.getTrangThai())) {
                return new ResponseObject<>(false, null, "Account is disabled");
            }

            // Tạo response data
            LoginResponseData responseData = new LoginResponseData();
            responseData.setId(nhanVien.getId());
            responseData.setMaNhanVien(nhanVien.getMaNhanVien());
            responseData.setTenNhanVien(nhanVien.getTenNhanVien());
            responseData.setTenTaiKhoan(nhanVien.getTenTaiKhoan());
            responseData.setEmail(nhanVien.getEmail());
            responseData.setIdQuyenHan(nhanVien.getIdQuyenHan().getId());
            responseData.setTenQuyenHan(nhanVien.getIdQuyenHan().getTenQuyenHan());
            responseData.setAccessToken(token);
            responseData.setRefreshToken(null); // Don't send refresh token in /me response

            return new ResponseObject<>(true, responseData, "User info retrieved successfully");

        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Error retrieving user info: " + e.getMessage());
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
}
