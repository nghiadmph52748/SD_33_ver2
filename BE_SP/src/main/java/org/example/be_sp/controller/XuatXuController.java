package org.example.be_sp.controller;

import org.example.be_sp.model.request.XuatXuRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.XuatXuService;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/xuat-xu-management")
@CrossOrigin(origins = "*")
public class XuatXuController {

    @Autowired
    XuatXuService xuatXuService;

    @GetMapping("/list")
    public ResponseObject<?> get() {
        return new ResponseObject<>(xuatXuService.getAll());
    }

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(xuatXuService.getAllXuatXu());
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(xuatXuService.pagingwithdeletedfalse(page, size), "Hien thi thanh cong");
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable Integer id) {
        return new ResponseObject<>(xuatXuService.getById(id));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody @Valid XuatXuRequest xuatXuRequest) {
        return new ResponseObject<>(true, xuatXuService.add(xuatXuRequest).getId(), "Thêm xuất xứ thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@RequestBody @Valid XuatXuRequest xuatXuRequest, @PathVariable Integer id) {
        xuatXuService.update(id, xuatXuRequest);
        return new ResponseObject<>(true, null, "Cập nhật xuất xứ thành công");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        xuatXuService.updateStatus(id);
        return new ResponseObject<>(true, null, "Cập nhật trạng thái xuất xứ thành công");
    }
}
