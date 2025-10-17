package org.example.be_sp.service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class NhanVienService {

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

    public NhanVien findByEmail(String email) {
        return nhanVienRepository.findByEmail(email).orElse(null);
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
            MultipartFile[] anhNhanVien = request.getAnhNhanVien();
            if (anhNhanVien != null && anhNhanVien.length > 0) {
                UploadImageToCloudinary uploadService = new UploadImageToCloudinary();
                ArrayList<UploadImageToCloudinary.Image> anhNhanVienUrl = uploadService.uploadImage(anhNhanVien);
                if (anhNhanVienUrl != null && !anhNhanVienUrl.isEmpty()) {
                    nv.setAnhNhanVien(anhNhanVienUrl.get(0).getUrl());
                }
            }
        } catch (Exception e) {
            throw new ApiException("Lỗi khi upload ảnh nhân viên: " + e.getMessage(), "400");
        }
        // set quyền hạn
        nv.setIdQuyenHan(repository.findById(request.getIdQuyenHan())
                .orElseThrow(() -> new ApiException("QuyenHan not found", "404")));

        // set tài khoản & mật khẩu
        if (request.getTenTaiKhoan() != null && request.getMatKhau() != null) {
            if (nhanVienRepository.existsByTenTaiKhoan(request.getTenTaiKhoan())) {
                throw new ApiException("TenTaiKhoan da ton tai", "400");
            }
            nv.setTenTaiKhoan(request.getTenTaiKhoan());
            nv.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        }

        nhanVienRepository.save(nv);
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
            MultipartFile[] anhNhanVien = request.getAnhNhanVien();
            if (anhNhanVien != null && anhNhanVien.length > 0) {
                UploadImageToCloudinary uploadService = new UploadImageToCloudinary();
                ArrayList<UploadImageToCloudinary.Image> anhNhanVienUrl = uploadService.uploadImage(anhNhanVien);
                if (anhNhanVienUrl != null && !anhNhanVienUrl.isEmpty()) {
                    nv.setAnhNhanVien(anhNhanVienUrl.get(0).getUrl());
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

        // update mật khẩu
        if (request.getMatKhau() != null) {
            nv.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
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

}
