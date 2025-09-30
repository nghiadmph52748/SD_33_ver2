package org.example.be_sp.controller;

import org.example.be_sp.model.request.KichThuocRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.KichThuocService;
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
@RequestMapping("/api/kich-thuoc-management")
@CrossOrigin(origins = "*")
public class KichThuocController {

    @Autowired
    KichThuocService kichThuocService;

    @GetMapping("/list")
    public ResponseObject<?> get() {
        return new ResponseObject<>(kichThuocService.getAll());
    }

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(kichThuocService.getAllKichThuoc());
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(kichThuocService.pagingwithdeletedfalse(page, size), "Hien thi thanh cong");
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable Integer id) {
        return new ResponseObject<>(kichThuocService.getById(id));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody KichThuocRequest kichThuocRequest) {
        kichThuocService.add(kichThuocRequest);
        return new ResponseObject<>(true, null, "Thêm kích thước thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody KichThuocRequest kichThuocRequest) {
        kichThuocService.update(id, kichThuocRequest);
        return new ResponseObject<>(true, null, "Cập nhật kích thước thành công");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        kichThuocService.updateStatus(id);
        return new ResponseObject<>(true, null, "Cập nhật trạng thái kích thước thành công");
    }
}
