package org.example.be_sp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.NhanVienRequest;
import org.example.be_sp.model.request.TrangThaiRequest;
import org.example.be_sp.model.response.NhanVienResponse;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/nhan-vien-management")
@CrossOrigin(origins = "*")
public class NhanVienController {
    @Autowired
    private NhanVienService nhanVienService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/playlist")
    public ResponseObject<?> getAllNhanVien() {
        try {
            return new ResponseObject<>(true, nhanVienService.getAllNhanVien(), "Lấy danh sách nhân viên thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy danh sách nhân viên: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> getNhanVienById(@PathVariable Integer id) {
        try {
            return new ResponseObject<>(true, nhanVienService.getNhanVienById(id), "Lấy thông tin nhân viên thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy thông tin nhân viên: " + e.getMessage());
        }
    }

    @GetMapping("/detail/email/{email}")
    public ResponseObject<?> getNhanVienByEmail(@PathVariable String email) {
        try {
            return new ResponseObject<>(true, nhanVienService.getNhanVienByEmail(email), "Lấy thông tin nhân viên theo email thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy thông tin nhân viên theo email: " + e.getMessage());
        }
    }

    @GetMapping("/detail/nickname/{tenTaiKhoan}")
    public ResponseObject<?> getNhanVienByTenTaiKhoan(@PathVariable String tenTaiKhoan) {
        try {
            return new ResponseObject<>(true, nhanVienService.getNhanVienByTenTaiKhoan(tenTaiKhoan), "Lấy thông tin nhân viên theo tên tài khoản thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy thông tin nhân viên theo tên tài khoản: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseObject<?> createNhanVien(@RequestBody NhanVienRequest request) {
        try {
            System.out.println("Ảnh nhân viên nhận được: " + request.getAnhNhanVien());  // check xem có nhận ảnh không
            nhanVienService.saveNhanVien(request, passwordEncoder);
            return new ResponseObject<>(true, null, "Thêm nhân viên mới thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi thêm nhân viên: " + e.getMessage());
        }
    }


    @PutMapping("/update/{id}")
    public ResponseObject<?> updateNhanVien(@PathVariable Integer id, @RequestBody NhanVienRequest request) {
        try {
            nhanVienService.updateNhanVien(id, request, passwordEncoder);
            return new ResponseObject<>(true, null, "Cập nhật nhân viên thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi cập nhật nhân viên: " + e.getMessage());
        }
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> update(@PathVariable Integer id) {
        try {
            nhanVienService.updateStatus(id);
            return new ResponseObject<>(true, null, "Cập nhật trạng thái nhân viên thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi cập nhật trạng thái nhân viên: " + e.getMessage());
        }
    }
    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=nhanvien.xlsx";
        response.setHeader(headerKey, headerValue);

        List<NhanVienResponse> nhanViens = nhanVienService.getAllNhanVien();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách nhân viên");

        // Tạo header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Mã NV");
        headerRow.createCell(1).setCellValue("Tên NV");
        headerRow.createCell(2).setCellValue("Tài khoản");
        headerRow.createCell(3).setCellValue("Email");
        headerRow.createCell(4).setCellValue("Số điện thoại");
        headerRow.createCell(5).setCellValue("Thành phố");
        headerRow.createCell(6).setCellValue("Trạng thái");

        // Ghi dữ liệu
        int rowCount = 1;
        for (NhanVienResponse nv : nhanViens) {
            Row row = sheet.createRow(rowCount++);
            row.createCell(0).setCellValue(nv.getMaNhanVien());
            row.createCell(1).setCellValue(nv.getTenNhanVien());
            row.createCell(2).setCellValue(nv.getTenTaiKhoan());
            row.createCell(3).setCellValue(nv.getEmail());
            row.createCell(4).setCellValue(nv.getSoDienThoai());
            row.createCell(5).setCellValue(nv.getThanhPho());
            row.createCell(6).setCellValue(nv.getTrangThai() != null && nv.getTrangThai() ? "Hoạt động" : "Không hoạt động");
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
    @PostMapping("/import")
    public ResponseObject<?> importNhanVien(@RequestParam("file") MultipartFile file) {
        try {
            nhanVienService.importNhanVienFromExcel(file, passwordEncoder);
            return new ResponseObject<>(true, null, "Import nhân viên thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi import: " + e.getMessage());
        }
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // Thiết lập content type và header
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=template_import_nhanvien.xlsx";
        response.setHeader(headerKey, headerValue);

        // Lấy file template từ resources (ví dụ file template bạn đã tạo sẵn)
        InputStream templateStream = getClass().getResourceAsStream("/template_import_nhanvien.xlsx");
        if (templateStream == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Gửi file về client
        IOUtils.copy(templateStream, response.getOutputStream());
        response.flushBuffer();
        templateStream.close();
    }

    @DeleteMapping("/nhan-vien/{id}")
    public ResponseEntity<String> deleteNhanVien(@PathVariable Integer id) {
        try {
            nhanVienService.deleteNhanVien(id);
            return ResponseEntity.ok("Nhân viên đã được xóa");
        } catch (ApiException e) {
            return ResponseEntity.status(404).body("Không tìm thấy nhân viên: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }
    @PutMapping("/nhan-vien/{id}/status")
    public ResponseObject<?> updateTrangThai(@PathVariable Integer id, @RequestBody TrangThaiRequest request) {
        try {
            nhanVienService.updateTrangThai(id, request.getTrangThai());
            return new ResponseObject<>(true, null, "Cập nhật trạng thái nhân viên thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi cập nhật trạng thái nhân viên: " + e.getMessage());
        }
    }

}
