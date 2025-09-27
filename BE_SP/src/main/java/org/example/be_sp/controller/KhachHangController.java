package org.example.be_sp.controller;

import org.example.be_sp.model.request.KhachHangRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/khach-hang-management")
@CrossOrigin(origins = "*")
public class KhachHangController {
    @Autowired
    private KhachHangService khachHangService;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(khachHangService.findAll());
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> getById(@PathVariable Integer id) {
        return new ResponseObject<>(khachHangService.findById(id));
    }

    @PostMapping("/add/quick")
    public ResponseObject<?> quickAdd(@RequestBody KhachHangRequest request) {
        khachHangService.quickAdd(request);
        return new ResponseObject<>(null, "Thêm khách hàng thành công!");
    }

    @PostMapping("/add")
    public ResponseObject<?> create(@RequestBody KhachHangRequest request) {
        khachHangService.save(request);
        return new ResponseObject<>(null, "Thêm khách hàng thành công!");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody KhachHangRequest request) {
        khachHangService.update(id, request);
        return new ResponseObject<>(null, "Cập nhật khách hàng thành công!");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        khachHangService.updateStatus(id);
        return new ResponseObject<>(null, "Xoá khách hàng thành công!");
    }
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