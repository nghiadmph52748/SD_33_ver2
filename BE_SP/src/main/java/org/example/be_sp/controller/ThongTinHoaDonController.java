package org.example.be_sp.controller;

import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.ThongTinHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/thong-tin-hoa-don-management")
@CrossOrigin(origins = "*")
public class ThongTinHoaDonController {
    @Autowired
    private ThongTinHoaDonService thongTinHoaDonService;
    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(thongTinHoaDonService.getAll(), "Hien thi thanh cong");
    }
    @GetMapping("/paging")
    public ResponseObject<?> phanTrang(@RequestParam(defaultValue = "0") Integer no,@RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(thongTinHoaDonService.phanTrang(no, size), "Phan trang thanh cong");
    }
    @GetMapping("/detail/{id}")
    public ResponseObject<?> getById(@PathVariable Integer id) {
        return new ResponseObject<>(thongTinHoaDonService.getById(id), "Hien thi chi tiet thanh cong");
    }
    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody org.example.be_sp.model.request.ThongTinHoaDonRequest thongTinHoaDonRequest) {
        thongTinHoaDonService.add(thongTinHoaDonRequest);
        return new ResponseObject<>(null, "Them moi thanh cong");
    }
    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody org.example.be_sp.model.request.ThongTinHoaDonRequest thongTinHoaDonRequest) {
        thongTinHoaDonService.update(id, thongTinHoaDonRequest);
        return new ResponseObject<>(null, "Cap nhat thanh cong");
    }
    @PutMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable Integer id) {
        thongTinHoaDonService.delete(id);
        return new ResponseObject<>(null, "Xoa thanh cong");
    }
}
