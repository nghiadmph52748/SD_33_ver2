package org.example.be_sp.controller;

import org.example.be_sp.model.request.PhuongThucThanhToanRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.PhuongThucThanhToanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/phuong-thuc-thanh-toan-management")
@CrossOrigin(origins = "*")
public class PhuongThucThanhToanController {
    @Autowired
    private PhuongThucThanhToanService phuongThucThanhToanService;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(phuongThucThanhToanService.getAll(), "Hiển thị thành công");
    }
    @GetMapping("/paging")
    public ResponseObject<?> getAllPaging(@RequestParam(defaultValue = "0") Integer page,@RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(phuongThucThanhToanService.paging(page, size), "Hiển thị thành công");
    }
    @GetMapping("/detail/{id}")
    public ResponseObject<?> getById(@PathVariable Integer id) {
        return new ResponseObject<>(phuongThucThanhToanService.getById(id), "Hiển thị thành công id: " + id);
    }
    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody PhuongThucThanhToanRequest phuongThucThanhToanRequest) {
        phuongThucThanhToanService.add(phuongThucThanhToanRequest);
        return new ResponseObject<>(true, null, "Thêm mới thành công");
    }
    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody PhuongThucThanhToanRequest phuongThucThanhToanRequest) {
        phuongThucThanhToanService.update(id, phuongThucThanhToanRequest);
        return new ResponseObject<>(true, null, "Cập nhật thành công id: "+ id);
    }
    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        phuongThucThanhToanService.updateStatus(id);
        return new ResponseObject<>(true, null, "Xóa phương thức thanh toán thành công id: " + id);
    }
}
