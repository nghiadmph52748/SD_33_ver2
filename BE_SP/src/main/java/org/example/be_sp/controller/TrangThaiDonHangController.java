package org.example.be_sp.controller;

import org.example.be_sp.model.request.TrangThaiDonHangRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.TrangThaiDonHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trang-thai-don-hang-management")
@CrossOrigin(origins = "*")
public class TrangThaiDonHangController {
    @Autowired
    TrangThaiDonHangService trangThaiDonHangService;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(trangThaiDonHangService.getAll(), "Hien thi thanh cong");
    }
    @GetMapping("/paging")
    public ResponseObject<?> phanTrang(@RequestParam(defaultValue = "0") Integer no,@RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(trangThaiDonHangService.paging(no, size), "Phan trang thanh cong");
    }
    @GetMapping("/detail/{id}")
    public ResponseObject<?> getByid(@PathVariable Integer id) {
        return new ResponseObject<>(trangThaiDonHangService.getById(id), "Hien thi chi tiet thanh cong");
    }
    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody TrangThaiDonHangRequest trangThaiDonHangRequest) {
        trangThaiDonHangService.update(id, trangThaiDonHangRequest);
        return new ResponseObject<>(true, null, "Cap nhat thanh cong");
    }
    @PutMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable Integer id) {
        trangThaiDonHangService.updateStatus(id);
        return new ResponseObject<>(true, null, "Xoa thanh cong");
    }
    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody TrangThaiDonHangRequest trangThaiDonHangRequest) {
        trangThaiDonHangService.add(trangThaiDonHangRequest);
        return new ResponseObject<>(true, null, "Them moi thanh cong");
    }

}
