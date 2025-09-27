package org.example.be_sp.controller;

import org.example.be_sp.model.request.BanHangTaiQuayRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hoa-don-management")
@CrossOrigin(origins = "*")
public class HoaDonController {
    @Autowired
    private HoaDonService hoaDonService;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(hoaDonService.getAll());
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(hoaDonService.phanTrang(page, size));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody BanHangTaiQuayRequest request) {
        hoaDonService.add(request);
        return new ResponseObject<>(null, "Thêm hóa đơn thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody BanHangTaiQuayRequest request) {
        hoaDonService.update(id, request);
        return new ResponseObject<>(null, "Cập nhật hóa đơn thành công");
    }

    @PutMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable Integer id) {
        hoaDonService.delete(id);
        return new ResponseObject<>(null, "Xóa hóa đơn thành công");
    }
}
