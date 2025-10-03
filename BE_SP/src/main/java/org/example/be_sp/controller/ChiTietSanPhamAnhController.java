package org.example.be_sp.controller;

import org.example.be_sp.model.request.ChiTietSanPhamAnhListRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.ChiTietSanPhamAnhService;
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
@RequestMapping("/api/chi-tiet-san-pham-anh-management")
@CrossOrigin(origins = "*")
public class ChiTietSanPhamAnhController {
    @Autowired
    ChiTietSanPhamAnhService service;

    @GetMapping({"/playlist", "/playlist.", "/playlist/"})
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(service.getAll());
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(service.paging(page, size));
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable Integer id) {
        return new ResponseObject<>(service.getById(id));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody ChiTietSanPhamAnhListRequest request) {
        service.add(request);
        return new ResponseObject<>(true, null, "Thêm mới thành công");
    }

    @PostMapping("/add-multiple")
    public ResponseObject<?> addMultiple(@RequestBody ChiTietSanPhamAnhListRequest request) {
        try {
            service.add(request);
            return new ResponseObject<>(true, "Thêm nhiều liên kết ảnh thành công", null);
        } catch (Exception e) {
            return new ResponseObject<>(false, "Lỗi khi thêm nhiều liên kết ảnh: " + e.getMessage(), null);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@RequestBody ChiTietSanPhamAnhListRequest request, @PathVariable Integer id) {
        service.update(id, request);
        return new ResponseObject<>(true, "Cập nhật thành công", null);
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        service.updateStatus(id);
        return new ResponseObject<>(true, "Xoá thành công", null);
    }
}
