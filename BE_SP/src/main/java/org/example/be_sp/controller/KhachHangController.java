package org.example.be_sp.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.example.be_sp.model.request.KhachHangRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/khach-hang-management")
@CrossOrigin(origins = "*")
public class KhachHangController {
    @Autowired
    private KhachHangService khachHangService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        try {
            return new ResponseObject<>(true, khachHangService.findAll(), "Lấy danh sách khách hàng thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy danh sách khách hàng: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/detail/{id}")
    public ResponseObject<?> getById(@PathVariable Integer id) {
        try {
            return new ResponseObject<>(true, khachHangService.findById(id), "Lấy thông tin khách hàng thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy thông tin khách hàng: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/add/quick")
    public ResponseObject<?> quickAdd(@RequestBody KhachHangRequest request) {
        try {
            khachHangService.quickAdd(request);
            return new ResponseObject<>(true, null, "Thêm khách hàng nhanh thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi thêm khách hàng nhanh: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/add")
    public ResponseObject<?> create(@RequestBody KhachHangRequest request) {
        try {
            khachHangService.save(request);
            return new ResponseObject<>(true, null, "Thêm khách hàng thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi thêm khách hàng: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody KhachHangRequest request) {
        try {
            khachHangService.update(id, request);
            return new ResponseObject<>(true, null, "Cập nhật khách hàng thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi cập nhật khách hàng: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        try {
            khachHangService.updateStatus(id);
            return new ResponseObject<>(true, null, "Cập nhật trạng thái khách hàng thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi cập nhật trạng thái khách hàng: " + e.getMessage());
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportToExcel() {
        try {
            ByteArrayInputStream in = khachHangService.exportKhachHangToExcel();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=khachhang.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(in.readAllBytes());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
