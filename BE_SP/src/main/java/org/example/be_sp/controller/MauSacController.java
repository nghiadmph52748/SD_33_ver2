package org.example.be_sp.controller;

import org.example.be_sp.model.request.MauSacRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.MauSacService;
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
@RequestMapping("/api/mau-sac-management")
@CrossOrigin(origins = "*")
public class MauSacController {

    @Autowired
    MauSacService mauSacService;

    @GetMapping("/list")
    public ResponseObject<?> get() {
        return new ResponseObject<>(mauSacService.getAll());
    }

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(mauSacService.getAllMauSac());
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return new ResponseObject<>(mauSacService.pagingwithdeletedfalse(page, size), "Hien thi thanh cong");
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable int id) {
        return new ResponseObject<>(mauSacService.getById(id));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody MauSacRequest mauSacRequest) {
        mauSacService.add(mauSacRequest);
        return new ResponseObject<>(true, null, "Thêm mới màu sắc thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody MauSacRequest mauSacRequest) {
        mauSacService.update(id, mauSacRequest);
        return new ResponseObject<>(true, null, "Cập nhật màu sắc thành công");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        mauSacService.updateStatus(id);
        return new ResponseObject<>(true, null, "Cập nhật trạng thái màu sắc thành công");
    }
}
