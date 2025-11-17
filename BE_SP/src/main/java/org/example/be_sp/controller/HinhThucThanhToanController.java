package org.example.be_sp.controller;

import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.HinhThucThanhToanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hinh-thuc-thanh-toan-management")
@CrossOrigin(origins = "*")
public class HinhThucThanhToanController {
    @Autowired
    private HinhThucThanhToanService hinhThucThanhToanService;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(hinhThucThanhToanService.getAll(), "Hien thi thanh cong");
    }
    @GetMapping("/paging")
    public ResponseObject<?> phanTrang(@RequestParam(defaultValue = "0") Integer no,@RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(hinhThucThanhToanService.phanTrang(no, size), "Phan trang thanh cong");
    }
    @GetMapping("/detail/{id}")
    public ResponseObject<?> getByid(@PathVariable Integer id) {
        return new ResponseObject<>(hinhThucThanhToanService.getByid(id), "Hien thi chi tiet thanh cong");
    }
    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody org.example.be_sp.model.request.HinhThucThanhToanRequest hinhThucThanhToanRequest) {
        hinhThucThanhToanService.add(hinhThucThanhToanRequest);
        return new ResponseObject<>(true, null, "Them moi thanh cong");
    }
    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody org.example.be_sp.model.request.HinhThucThanhToanRequest hinhThucThanhToanRequest) {
        hinhThucThanhToanService.update(id, hinhThucThanhToanRequest);
        return new ResponseObject<>(true, null, "Cap nhat thanh cong");
    }
    @PutMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable Integer id) {
        hinhThucThanhToanService.trangThai(id);
        return new ResponseObject<>(true, null, "Xoa thanh cong");
    }
    @GetMapping("/by-hoa-don/{hoaDonId}")
    public ResponseObject<?> getByHoaDonId(@PathVariable Integer hoaDonId) {
        return new ResponseObject<>(hinhThucThanhToanService.getByHoaDonId(hoaDonId), "Lấy hình thức thanh toán theo hóa đơn thành công");
    }
}
