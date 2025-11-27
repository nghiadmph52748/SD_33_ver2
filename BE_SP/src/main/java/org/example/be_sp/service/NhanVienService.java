package org.example.be_sp.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.NhanVienRequest;
import org.example.be_sp.model.response.NhanVienResponse;
import org.example.be_sp.repository.NhanVienRepository;
import org.example.be_sp.repository.QuyenHanRepository;
import org.example.be_sp.service.upload.UploadImageToCloudinary;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class NhanVienService {
    @Autowired
    private Emailgui emailService;
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    QuyenHanRepository repository;

    public List<NhanVienResponse> getAllNhanVien() {
        return nhanVienRepository.findAll().stream().map(NhanVienResponse::new).toList();
    }

    public NhanVienResponse getNhanVienById(Integer id) {
        return nhanVienRepository.findById(id).map(NhanVienResponse::new).orElseThrow(() -> new ApiException("NhanVien not found", "404"));
    }

    public NhanVienResponse getNhanVienByEmail(String email) {
        return nhanVienRepository.findByEmail(email).map(NhanVienResponse::new).orElseThrow(() -> new ApiException("NhanVien not found", "404"));
    }

    public NhanVienResponse getNhanVienByTenTaiKhoan(String tenTaiKhoan) {
        return nhanVienRepository.findByTenTaiKhoan(tenTaiKhoan).map(NhanVienResponse::new).orElseThrow(() -> new ApiException("NhanVien not found", "404"));
    }

    public NhanVien findByTenTaiKhoan(String tenTaiKhoan) {
        return nhanVienRepository.findByTenTaiKhoan(tenTaiKhoan).orElse(null);
    }

    public void saveNhanVien(NhanVienRequest request, PasswordEncoder passwordEncoder) {
        NhanVien nv = new NhanVien();
        nv.setTenNhanVien(request.getTenNhanVien());
        nv.setEmail(request.getEmail());
        nv.setSoDienThoai(request.getSoDienThoai());
        nv.setDiaChiCuThe(request.getDiaChiCuThe());
        nv.setThanhPho(request.getThanhPho());
        nv.setQuan(request.getQuan());
        nv.setPhuong(request.getPhuong());
        nv.setCccd(request.getCccd());
        nv.setNgaySinh(request.getNgaySinh());
        nv.setTrangThai(request.getTrangThai());
        nv.setDeleted(false);
        nv.setGioiTinh(request.getGioiTinh());
        nv.setCreateAt(request.getCreateAt());
        nv.setCreateBy(request.getCreateBy());
        try {
            if (request.getAnhNhanVienUrl() != null && !request.getAnhNhanVienUrl().isEmpty()) {
                nv.setAnhNhanVien(request.getAnhNhanVienUrl());
            } else {
                MultipartFile[] anhNhanVien = request.getAnhNhanVien();
                if (anhNhanVien != null && anhNhanVien.length > 0) {
                    UploadImageToCloudinary uploadService = new UploadImageToCloudinary();
                    ArrayList<UploadImageToCloudinary.Image> anhNhanVienUrl = uploadService.uploadImage(anhNhanVien);
                    if (anhNhanVienUrl != null && !anhNhanVienUrl.isEmpty()) {
                        nv.setAnhNhanVien(anhNhanVienUrl.get(0).getUrl());
                    }
                }
            }
        } catch (Exception e) {
            throw new ApiException("Lỗi khi upload ảnh nhân viên: " + e.getMessage(), "400");
        }
        // set quyền hạn
        nv.setIdQuyenHan(repository.findById(request.getIdQuyenHan())
                .orElseThrow(() -> new ApiException("QuyenHan not found", "404")));


        // Sinh mật khẩu ngẫu nhiên nếu không có trong request
        String rawPassword = request.getMatKhau();
        if (rawPassword == null || rawPassword.isEmpty()) {
            rawPassword = generateRandomPassword();
        }

        // Kiểm tra trùng tài khoản
        if (nhanVienRepository.existsByTenTaiKhoan(request.getTenTaiKhoan())) {
            throw new ApiException("Tên tài khoản đã tồn tại", "400");
        }

        // Thiết lập tài khoản và mật khẩu (chỉ mã hóa 1 lần)
        nv.setTenTaiKhoan(request.getTenTaiKhoan());
        nv.setMatKhau(passwordEncoder.encode(rawPassword));

        nhanVienRepository.save(nv);

        try {
            System.out.println("📧 Đang gửi email thông tin tài khoản đến: " + nv.getEmail());
            emailService.sendAccountInfo(
                    nv.getEmail(),
                    nv.getTenNhanVien(),
                    rawPassword // gửi mật khẩu gốc (chưa mã hóa)
            );
            System.out.println("✅ Email đã được gửi thành công đến: " + nv.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Gửi mail thất bại: " + e.getMessage());
            e.printStackTrace();
        }

    }
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    public NhanVienResponse updateNhanVien(Integer id, NhanVienRequest request, PasswordEncoder passwordEncoder) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên với id: " + id, "404"));

        if (request.getTenNhanVien() != null) {
            nv.setTenNhanVien(request.getTenNhanVien());
        }
        if (request.getEmail() != null) {
            nv.setEmail(request.getEmail());
        }
        if (request.getSoDienThoai() != null) {
            nv.setSoDienThoai(request.getSoDienThoai());
        }
        if (request.getDiaChiCuThe() != null) {
            nv.setDiaChiCuThe(request.getDiaChiCuThe());
        }
        if (request.getThanhPho() != null) {
            nv.setThanhPho(request.getThanhPho());
        }
        if (request.getQuan() != null) {
            nv.setQuan(request.getQuan());
        }
        if (request.getPhuong() != null) {
            nv.setPhuong(request.getPhuong());
        }
        if (request.getCccd() != null) {
            nv.setCccd(request.getCccd());
        }
        if (request.getNgaySinh() != null) {
            nv.setNgaySinh(request.getNgaySinh());
        }
        if (request.getTrangThai() != null) {
            nv.setTrangThai(request.getTrangThai());
        }
        if (request.getDeleted() != null) {
            nv.setDeleted(request.getDeleted());
        }
        if (request.getGioiTinh() != null) {
            nv.setGioiTinh(request.getGioiTinh());
        }
        // Upload ảnh mới nếu có
        try {
            if (request.getAnhNhanVienUrl() != null && !request.getAnhNhanVienUrl().isEmpty()) {
                nv.setAnhNhanVien(request.getAnhNhanVienUrl());
            } else {
                MultipartFile[] anhNhanVien = request.getAnhNhanVien();
                if (anhNhanVien != null && anhNhanVien.length > 0) {
                    UploadImageToCloudinary uploadService = new UploadImageToCloudinary();
                    ArrayList<UploadImageToCloudinary.Image> anhNhanVienUrl = uploadService.uploadImage(anhNhanVien);
                    if (anhNhanVienUrl != null && !anhNhanVienUrl.isEmpty()) {
                        nv.setAnhNhanVien(anhNhanVienUrl.get(0).getUrl());
                    }
                }
            }
        } catch (Exception e) {
            throw new ApiException("Lỗi khi upload ảnh nhân viên: " + e.getMessage(), "400");
        }

        // update quyền hạn
        if (request.getIdQuyenHan() != null) {
            nv.setIdQuyenHan(repository.findById(request.getIdQuyenHan())
                    .orElseThrow(() -> new ApiException("Không tìm thấy quyền hạn với id: " + request.getIdQuyenHan(), "404")));
        }

        // update tài khoản
        if (request.getTenTaiKhoan() != null) {
            nv.setTenTaiKhoan(request.getTenTaiKhoan());
        }

        // Track if password or email changed for email notification
        boolean passwordChanged = false;
        String rawPassword = null;
        String oldEmail = nv.getEmail();
        boolean emailChanged = false;
        
        // update mật khẩu - generate random password if not provided to always send email
        if (request.getMatKhau() != null && !request.getMatKhau().isEmpty()) {
            String inputPassword = request.getMatKhau();
            
            // Kiểm tra xem password có phải là bcrypt hash không (bcrypt hash luôn bắt đầu bằng $2a$, $2b$, hoặc $2y$)
            boolean isBcryptHash = inputPassword.startsWith("$2a$") || 
                                   inputPassword.startsWith("$2b$") || 
                                   inputPassword.startsWith("$2y$");
            
            if (isBcryptHash) {
                // Nếu là bcrypt hash, có nghĩa là frontend gửi lại password từ database
                // Không cập nhật password, chỉ generate password mới để gửi email
                System.out.println("⚠️ [updateNhanVien] Phát hiện password đã mã hóa từ frontend, bỏ qua và tạo password mới");
                rawPassword = generateRandomPassword();
                nv.setMatKhau(passwordEncoder.encode(rawPassword));
                passwordChanged = true;
                System.out.println("🔑 [updateNhanVien] Mật khẩu ngẫu nhiên đã được tạo: " + rawPassword);
            } else {
                // Nếu là plain text, đây là password mới từ user
                rawPassword = inputPassword;
                nv.setMatKhau(passwordEncoder.encode(rawPassword));
                passwordChanged = true;
                System.out.println("🔑 [updateNhanVien] Mật khẩu mới đã được cập nhật");
            }
        } else {
            // Generate random password if not provided during update - always send email with new password
            rawPassword = generateRandomPassword();
            nv.setMatKhau(passwordEncoder.encode(rawPassword));
            passwordChanged = true;
            System.out.println("🔑 [updateNhanVien] Mật khẩu ngẫu nhiên đã được tạo: " + rawPassword);
        }

        // Check if email changed
        if (request.getEmail() != null && !request.getEmail().equals(oldEmail)) {
            emailChanged = true;
        }

        if (request.getCreateAt() != null) {
            nv.setCreateAt(request.getCreateAt());
        }
        if (request.getCreateBy() != null) {
            nv.setCreateBy(request.getCreateBy());
        }

        if (request.getUpdateAt() != null) {
            nv.setUpdateAt(request.getUpdateAt());
        }
        if (request.getUpdateBy() != null) {
            nv.setUpdateBy(request.getUpdateBy());
        }
        nhanVienRepository.save(nv);
        
        // Always send email notification with password when updating employee
        try {
            if (passwordChanged && rawPassword != null) {
                System.out.println("📧 [updateNhanVien] Đang gửi email thông tin tài khoản cập nhật đến: " + nv.getEmail());
                emailService.sendAccountInfo(
                        nv.getEmail(),
                        nv.getTenNhanVien(),
                        rawPassword
                );
                System.out.println("✅ [updateNhanVien] Email thông tin tài khoản đã được gửi thành công!");
            }
            
            // Also send notification if email changed
            if (emailChanged && oldEmail != null) {
                System.out.println("📧 [updateNhanVien] Email đã được thay đổi. Gửi thông báo đến email mới: " + nv.getEmail());
                String subject = "Thông báo thay đổi email tài khoản";
                String body = "Xin chào " + nv.getTenNhanVien() + ",\n\n"
                        + "Email tài khoản của bạn đã được cập nhật từ " + oldEmail + " thành " + nv.getEmail() + ".\n\n"
                        + "Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ quản trị viên ngay lập tức.\n\n"
                        + "Trân trọng,\nĐội ngũ hỗ trợ hệ thống.";
                emailService.sendEmail(nv.getEmail(), subject, body);
                System.out.println("✅ [updateNhanVien] Email thông báo thay đổi đã được gửi thành công!");
            }
        } catch (Exception e) {
            System.err.println("❌ [updateNhanVien] Gửi mail thất bại: " + e.getMessage());
            e.printStackTrace();
        }
        
        return new NhanVienResponse(nv);
    }

    public boolean existsByEmail(String email) {
        return nhanVienRepository.existsByEmail(email);
    }

    public boolean existsByTenTaiKhoan(String tenTaiKhoan) {
        return nhanVienRepository.existsByTenTaiKhoan(tenTaiKhoan);
    }

    public void updateStatus(Integer id) {
        NhanVien c = nhanVienRepository.findById(id).orElseThrow(() -> new ApiException("NhanVien not found", "404"));
        c.setDeleted(true);
        nhanVienRepository.save(c);
    }

    public void importNhanVienFromExcel(MultipartFile file, PasswordEncoder passwordEncoder) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;

            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;  // Bỏ qua header
                    continue;
                }

                NhanVienRequest request = new NhanVienRequest();

                request.setTenNhanVien(getCellStringValue(row.getCell(0)));
                request.setTenTaiKhoan(getCellStringValue(row.getCell(1)));

                // Mật khẩu mặc định khi import
                request.setMatKhau("123456");

                request.setEmail(getCellStringValue(row.getCell(2)));
                request.setSoDienThoai(getCellStringValue(row.getCell(3)));
                request.setAnhNhanVien(null); // Bạn có thể cập nhật nếu có cột ảnh trong Excel

                Cell ngaySinhCell = row.getCell(4);
                if (ngaySinhCell != null && ngaySinhCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(ngaySinhCell)) {
                    Date date = ngaySinhCell.getDateCellValue();
                    request.setNgaySinh(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                }

                request.setThanhPho(getCellStringValue(row.getCell(5)));
                request.setQuan(getCellStringValue(row.getCell(6)));
                request.setPhuong(getCellStringValue(row.getCell(7)));
                request.setDiaChiCuThe(getCellStringValue(row.getCell(8)));
                request.setCccd(getCellStringValue(row.getCell(9)));

                // idQuyenHan mặc định hoặc lấy từ Excel (nếu có cột)
                request.setIdQuyenHan(1);

                String trangThaiStr = getCellStringValue(row.getCell(10));
                request.setTrangThai("Hoạt động".equalsIgnoreCase(trangThaiStr));

                request.setDeleted(false);

                if (!existsByTenTaiKhoan(request.getTenTaiKhoan()) && !existsByEmail(request.getEmail())) {
                    saveNhanVien(request, passwordEncoder);
                }
                // Hoặc bạn có thể chọn cập nhật nếu tồn tại, tuỳ yêu cầu
            }
        }
    }

    public boolean existsById(Integer id) {
        return nhanVienRepository.existsById(id);  // Trả về true nếu tồn tại, false nếu không
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return null;
    }

    public void deleteNhanVien(Integer id) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên với id: " + id, "404"));

        nhanVienRepository.delete(nv);  // Xóa cứng khỏi database
    }

    public void updateTrangThai(Integer id, Boolean trangThai) {
        Optional<NhanVien> optionalNhanVien = nhanVienRepository.findById(id);
        if (!optionalNhanVien.isPresent()) {
            throw new ApiException("Không tìm thấy nhân viên với ID: " + id, "NOT_FOUND");
        }

        NhanVien nhanVien = optionalNhanVien.get();
        nhanVien.setTrangThai(trangThai);
        nhanVienRepository.save(nhanVien);
    }


    public void forgotPassword(String email) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên với email: " + email, "404"));

        // Tạo token reset
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        // token có hiệu lực 1 giờ

        nhanVien.setResetToken(token);
        nhanVien.setResetTokenExpiry(expiry);
        nhanVienRepository.save(nhanVien);

        // Gửi email có link reset
        String resetLink = "http://localhost:5173/auth/reset-password?token=" + token;
        String subject = "Yêu cầu đặt lại mật khẩu";
        String body = "Xin chào " + nhanVien.getTenNhanVien() + ",\n\n"
                + "Bạn đã yêu cầu đặt lại mật khẩu. Hãy nhấp vào liên kết bên dưới để đổi mật khẩu:\n"
                + resetLink + "\n\n"
                + "Liên kết này sẽ hết hạn sau 15 phút.\n\n"
                + "Trân trọng,\nĐội ngũ hỗ trợ.";

        emailService.sendEmail(nhanVien.getEmail(), subject, body);
    }

    public void resetPassword(String token, String newPassword, PasswordEncoder passwordEncoder) {
        NhanVien nhanVien = nhanVienRepository.findByResetToken(token)
                .orElseThrow(() -> new ApiException("Token không hợp lệ hoặc đã hết hạn", "400"));

        if (nhanVien.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ApiException("Token đã hết hạn. Vui lòng yêu cầu lại.", "400");
        }

        nhanVien.setMatKhau(passwordEncoder.encode(newPassword));
        nhanVien.setResetToken(null);
        nhanVien.setResetTokenExpiry(null);
        nhanVienRepository.save(nhanVien);
    }

    public Map<String, Object> verifyToken(String token) {
        Map<String, Object> result = new HashMap<>();
        Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByResetToken(token);

        if (nhanVienOpt.isEmpty()) {
            result.put("valid", false);
            result.put("expired", true);
            return result;
        }

        NhanVien nhanVien = nhanVienOpt.get();

        boolean expired = nhanVien.getResetTokenExpiry().isBefore(LocalDateTime.now());
        result.put("valid", !expired);
        result.put("expired", expired);
        return result;
    }

    public void updateNhanVienDirectly(NhanVien nhanVien) {
        nhanVienRepository.save(nhanVien);
    }

}
