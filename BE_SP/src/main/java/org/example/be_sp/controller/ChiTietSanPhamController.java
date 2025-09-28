package org.example.be_sp.controller;

import org.example.be_sp.model.request.ChiTietSanPhamRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.ChiTietSanPhamService;
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
@RequestMapping("/api/chi-tiet-san-pham-management")
@CrossOrigin(origins = "*")
public class ChiTietSanPhamController {

    @Autowired
    ChiTietSanPhamService service;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(service.getAll());
    }

    @GetMapping("/list/{id}")
    public ResponseObject<?> getAllByIdSanPham(@PathVariable Integer id) {
        return new ResponseObject<>(service.getAllByIdSanPham(id));
    }

    @GetMapping("/paging/all")
    public ResponseObject<?> getAllWithPage(@RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return new ResponseObject<>(service.getAllWithPage(page, size));
    }

    @GetMapping("/paging/{idSanPham}")
    public ResponseObject<?> paging(@PathVariable("idSanPham") Integer idSanPham,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return new ResponseObject<>(service.getAllByIdSanPhamWithPage(idSanPham, page, size));
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable Integer id) {
        return new ResponseObject<>(service.getById(id));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody ChiTietSanPhamRequest request) {
        Integer id = service.add(request);
        return new ResponseObject<>(true, id, "Thêm chi tiết sản phẩm thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@RequestBody ChiTietSanPhamRequest request, @PathVariable Integer id) {
        service.update(request, id);
        return new ResponseObject<>(true, null, "Cập nhật chi tiết sản phẩm thành công");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        service.updateStatus(id);
        return new ResponseObject<>(true, null, "Cập nhật trạng thái chi tiết sản phẩm thành công");
    }
}
