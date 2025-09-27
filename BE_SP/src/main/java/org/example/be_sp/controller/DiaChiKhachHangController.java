package org.example.be_sp.controller;

import org.example.be_sp.model.request.DiaChiKhachHangRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.DiaChiKhachHangService;
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
@RequestMapping("/api/dia-chi-khach-hang-management")
@CrossOrigin(origins = "*")
public class DiaChiKhachHangController {
    @Autowired
    DiaChiKhachHangService service;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(service.getAll());
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(service.paging(page, size));
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable Integer id) {
        return new ResponseObject<>(service.getById(id));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody DiaChiKhachHangRequest request) {
        service.add(request);
        return new ResponseObject<>(null, "Thêm địa chỉ khách hàng thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@RequestBody DiaChiKhachHangRequest request, @PathVariable Integer id) {
        service.update(id, request);
        return new ResponseObject<>(null, "Cập nhật địa chỉ khách hàng thành công");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        service.updateStatus(id);
        return new ResponseObject<>(null, "Cập nhật trạng thái địa chỉ khách hàng thành công");
    }
}
