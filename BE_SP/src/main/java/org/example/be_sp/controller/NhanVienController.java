package org.example.be_sp.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.be_sp.model.request.NhanVienRequest;
import org.example.be_sp.model.response.NhanVienResponse;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien-management")
@CrossOrigin(origins = "*")
public class NhanVienController {
    @Autowired
    private NhanVienService nhanVienService;

    @GetMapping("/playlist")
    public ResponseObject<?> getAllNhanVien() {
        return new ResponseObject<>(nhanVienService.getAllNhanVien());
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> getNhanVienById(@PathVariable Integer id) {
        return new ResponseObject<>(nhanVienService.getNhanVienById(id));
    }

    @GetMapping("/detail/email/{email}")
    public ResponseObject<?> getNhanVienByEmail(@PathVariable String email) {
        return new ResponseObject<>(nhanVienService.getNhanVienByEmail(email));
    }

    @GetMapping("/detail/nickname/{tenTaiKhoan}")
    public ResponseObject<?> getNhanVienByTenTaiKhoan(@PathVariable String tenTaiKhoan) {
        return new ResponseObject<>(nhanVienService.getNhanVienByTenTaiKhoan(tenTaiKhoan));
    }

    @PostMapping("/add")
    public ResponseObject<?> createNhanVien(@RequestBody NhanVienRequest request) {
        nhanVienService.saveNhanVien(request);
        return new ResponseObject<>(null, "Add success");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> updateNhanVien(@PathVariable Integer id, @RequestBody NhanVienRequest request) {
        nhanVienService.updateNhanVien(id, request);
        return new ResponseObject<>(null, "Update success");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> update(@PathVariable Integer id) {
        nhanVienService.updateStatus(id);
        return new ResponseObject<>(null, "Update status success");
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
            nhanVienService.importNhanVienFromExcel(file);
            return new ResponseObject<>(null, "Import nhân viên thành công");
        } catch (Exception e) {
            return new ResponseObject<>(null, "Lỗi import: " + e.getMessage());
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

}
