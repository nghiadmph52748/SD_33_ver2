package org.example.be_sp.controller;

import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.HinhThucChuyenKhoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hinh-thuc-thanh-toan-management")
@CrossOrigin(origins = "*")
public class HinhThucThanhToanController {
    @Autowired
    private HinhThucChuyenKhoanService hinhThucChuyenKhoanService;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(hinhThucChuyenKhoanService.getAll(), "Hien thi thanh cong");
    }
    @GetMapping("/paging")
    public ResponseObject<?> phanTrang(@RequestParam(defaultValue = "0") Integer no,@RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(hinhThucChuyenKhoanService.phanTrang(no, size), "Phan trang thanh cong");
    }
    @GetMapping("/detail/{id}")
    public ResponseObject<?> getByid(@PathVariable Integer id) {
        return new ResponseObject<>(hinhThucChuyenKhoanService.getByid(id), "Hien thi chi tiet thanh cong");
    }
    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody org.example.be_sp.model.request.HinhThucThanhToanRequest hinhThucThanhToanRequest) {
        hinhThucChuyenKhoanService.add(hinhThucThanhToanRequest);
        return new ResponseObject<>(true, null, "Them moi thanh cong");
    }
    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody org.example.be_sp.model.request.HinhThucThanhToanRequest hinhThucThanhToanRequest) {
        hinhThucChuyenKhoanService.update(id, hinhThucThanhToanRequest);
        return new ResponseObject<>(true, null, "Cap nhat thanh cong");
    }
    @PutMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable Integer id) {
        hinhThucChuyenKhoanService.trangThai(id);
        return new ResponseObject<>(true, null, "Xoa thanh cong");
    }
}
