package org.example.be_sp.service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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
import org.example.be_sp.util.MapperUtils;
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

    public void saveNhanVien(NhanVienRequest request, PasswordEncoder passwordEncoder) {
        NhanVien nv = MapperUtils.map(request, NhanVien.class);
        nv.setIdQuyenHan(repository.findById(request.getIdQuyenHan()).orElseThrow(() -> new ApiException("QuyenHan not found", "404")));
        if (request.getTenTaiKhoan() != null && request.getMatKhau() != null){
            if (nhanVienRepository.existsByTenTaiKhoan(request.getTenTaiKhoan())){
                throw new ApiException("TenTaiKhoan da ton tai", "400");
            } else {
                nv.setTenTaiKhoan(request.getTenTaiKhoan());
                nv.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
            }
        }
        nhanVienRepository.save(nv);
    }

    public void updateNhanVien(Integer id, NhanVienRequest request, PasswordEncoder passwordEncoder) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ApiException("NhanVien not found", "404"));

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
        nv.setDeleted(request.getDeleted());
        nv.setGioiTinh(request.getGioiTinh());
        // 👇 update ảnh nhân viên
        if (request.getAnhNhanVien() != null) {
            nv.setAnhNhanVien(request.getAnhNhanVien());
        }

        // update quyền hạn
        nv.setIdQuyenHan(repository.findById(request.getIdQuyenHan())
                .orElseThrow(() -> new ApiException("QuyenHan not found", "404")));

        // update tài khoản
        if (request.getTenTaiKhoan() != null) {
            nv.setTenTaiKhoan(request.getTenTaiKhoan());
        }

        // update mật khẩu
        if (request.getMatKhau() != null) {
            nv.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        }

        nhanVienRepository.save(nv);
    }


    public boolean existsByEmail(String email) {
        return nhanVienRepository.existsByEmail(email);
    }

    public boolean existsByTenTaiKhoan(String tenTaiKhoan) {
        return nhanVienRepository.existsByTenTaiKhoan(tenTaiKhoan);
    }

    public void updateStatus(Integer id){
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


    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return null;
    }
}
