package org.example.be_sp.controller;

import java.util.List;
import java.util.Map;

import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.entity.DiaChiKhachHang;
import org.example.be_sp.model.request.CustomerLoginRequest;
import org.example.be_sp.model.request.CustomerRefreshTokenRequest;
import org.example.be_sp.model.request.CustomerRegisterRequest;
import org.example.be_sp.model.request.OAuthLoginRequest;
import org.example.be_sp.model.request.UpdateCustomerProfileRequest;
import org.example.be_sp.model.DiaChi;
import org.example.be_sp.model.response.CustomerLoginResponse;
import org.example.be_sp.model.response.CustomerProfileResponse;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.repository.DiaChiKhachHangRepository;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.security.JwtUtils;
import org.example.be_sp.service.OAuthService;
import org.example.be_sp.service.TokenBlacklistService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final OAuthService oAuthService;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;

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

    @PostMapping("/register")
    public ResponseObject<CustomerLoginResponse> register(@RequestBody CustomerRegisterRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()
                || request.getFullName() == null || request.getFullName().isBlank()) {
            return new ResponseObject<>(false, null, "Vui lòng nhập đầy đủ họ tên, email và mật khẩu");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (khachHangRepository.findByEmail(email) != null) {
            return new ResponseObject<>(false, null, "Email đã được sử dụng");
        }

        KhachHang kh = new KhachHang();
        kh.setTenKhachHang(request.getFullName().trim());
        kh.setEmail(email);
        kh.setTenTaiKhoan(email);
        kh.setMatKhau(passwordEncoder.encode(request.getPassword()));
        kh.setTrangThai(true);
        kh.setDeleted(false);
        kh.setPhanLoai(0);

        KhachHang saved = khachHangRepository.save(kh);

        String subject = saved.getTenTaiKhoan() != null && !saved.getTenTaiKhoan().isBlank()
                ? saved.getTenTaiKhoan()
                : saved.getEmail();

        List<String> roles = List.of("customer");
        String accessToken = jwtUtils.generateToken(subject, roles);
        String refreshToken = jwtUtils.generateRefreshToken(subject);

        CustomerProfileResponse profile = new CustomerProfileResponse(saved);
        CustomerLoginResponse response = new CustomerLoginResponse(accessToken, refreshToken, profile);
        return new ResponseObject<>(true, response, "Đăng ký tài khoản thành công");
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
            profile.setListDiaChi(mapAddresses(khachHang.getId()));
            return ResponseEntity.ok(new ResponseObject<>(true, profile, "Thông tin khách hàng"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ResponseObject<>(false, null, "Token không hợp lệ"));
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ResponseObject<CustomerProfileResponse>> updateProfile(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody UpdateCustomerProfileRequest request) {
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

            // Update profile fields
            if (request.getTenKhachHang() != null && !request.getTenKhachHang().isBlank()) {
                khachHang.setTenKhachHang(request.getTenKhachHang().trim());
            }
            if (request.getSoDienThoai() != null && !request.getSoDienThoai().isBlank()) {
                khachHang.setSoDienThoai(request.getSoDienThoai().trim());
            }
            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                khachHang.setEmail(request.getEmail().trim().toLowerCase());
            }

            // Cập nhật hoặc tạo địa chỉ mặc định nếu có thông tin địa chỉ gửi lên
            if (request.getThanhPho() != null || request.getQuan() != null
                    || request.getPhuong() != null || request.getDiaChiCuThe() != null) {

                DiaChiKhachHang diaChi = diaChiKhachHangRepository.findAllByIdKhachHang_Id(khachHang.getId())
                        .stream()
                        .filter(dc -> !Boolean.TRUE.equals(dc.getDeleted()))
                        .findFirst()
                        .orElseGet(() -> {
                            DiaChiKhachHang dcNew = new DiaChiKhachHang();
                            dcNew.setIdKhachHang(khachHang);
                            dcNew.setTenDiaChi("Địa chỉ mặc định");
                            dcNew.setMacDinh(true);
                            dcNew.setTrangThai(true);
                            dcNew.setDeleted(false);
                            return dcNew;
                        });

                if (request.getThanhPho() != null && !request.getThanhPho().isBlank()) {
                    diaChi.setThanhPho(request.getThanhPho().trim());
                }
                if (request.getQuan() != null && !request.getQuan().isBlank()) {
                    diaChi.setQuan(request.getQuan().trim());
                }
                if (request.getPhuong() != null && !request.getPhuong().isBlank()) {
                    diaChi.setPhuong(request.getPhuong().trim());
                }
                if (request.getDiaChiCuThe() != null && !request.getDiaChiCuThe().isBlank()) {
                    diaChi.setDiaChiCuThe(request.getDiaChiCuThe().trim());
                }

                diaChiKhachHangRepository.save(diaChi);
            }

            khachHangRepository.save(khachHang);

            CustomerProfileResponse profile = new CustomerProfileResponse(khachHang);
            profile.setListDiaChi(mapAddresses(khachHang.getId()));
            return ResponseEntity.ok(new ResponseObject<>(true, profile, "Cập nhật thông tin thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseObject<>(false, null, "Lỗi khi cập nhật thông tin: " + e.getMessage()));
        }
    }

    @PostMapping("/oauth/login")
    public ResponseObject<CustomerLoginResponse> oauthLogin(@RequestBody OAuthLoginRequest request) {
        if (request == null || request.getToken() == null || request.getToken().isBlank()
                || request.getProvider() == null || request.getProvider().isBlank()) {
            return new ResponseObject<>(false, null, "Thiếu thông tin đăng nhập OAuth");
        }

        OAuthService.OAuthUserInfo userInfo;
        if ("google".equalsIgnoreCase(request.getProvider())) {
            userInfo = oAuthService.verifyGoogleToken(request.getToken());
        } else {
            return new ResponseObject<>(false, null, "Nhà cung cấp OAuth không được hỗ trợ");
        }

        if (userInfo == null) {
            return new ResponseObject<>(false, null, "Xác thực OAuth thất bại. Vui lòng thử lại.");
        }

        KhachHang khachHang = oAuthService.findOrCreateUser(userInfo);
        if (khachHang == null) {
            return new ResponseObject<>(false, null, "Không thể tạo hoặc tìm thấy tài khoản");
        }

        if (!Boolean.TRUE.equals(khachHang.getTrangThai())) {
            return new ResponseObject<>(false, null, "Tài khoản đã bị khóa");
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

    private List<DiaChi> mapAddresses(Integer khachHangId) {
        return diaChiKhachHangRepository.findAllByIdKhachHang_Id(khachHangId)
                .stream()
                .filter(dc -> !Boolean.TRUE.equals(dc.getDeleted()))
                .map(dc -> new DiaChi(
                        dc.getTenDiaChi(),
                        dc.getDiaChiCuThe(),
                        dc.getThanhPho(),
                        dc.getQuan(),
                        dc.getPhuong(),
                        dc.getMacDinh()
                ))
                .toList();
    }
}


