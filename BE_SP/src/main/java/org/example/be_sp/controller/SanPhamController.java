package org.example.be_sp.controller;

import org.example.be_sp.model.request.SanPhamRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.SanPhamService;
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
@RequestMapping("/api/san-pham-management")
@CrossOrigin(origins = "*")
public class SanPhamController {
    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        try {
            return new ResponseObject<>(true, sanPhamService.getAll(), "Lấy danh sách sản phẩm thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    @GetMapping("/paging")
    public ResponseObject<?> getAllPaging(@RequestParam(defaultValue = "0", name="page") Integer page, @RequestParam(defaultValue = "10", name="size") Integer size) {
        try {
            return new ResponseObject<>(true, sanPhamService.paging(page, size), "Lấy danh sách sản phẩm phân trang thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy danh sách sản phẩm phân trang: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> getById(@PathVariable Integer id) {
        try {
            return new ResponseObject<>(true, sanPhamService.getById(id), "Lấy thông tin sản phẩm thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy thông tin sản phẩm: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody SanPhamRequest request) {
        try {
            return new ResponseObject<>(true, sanPhamService.add(request), "Thêm sản phẩm mới thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody SanPhamRequest request) {
        try {
            return new ResponseObject<>(true, sanPhamService.update(request, id), "Cập nhật sản phẩm thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        try {
            sanPhamService.updateStatus(id);
            return new ResponseObject<>(true, null, "Cập nhật trạng thái sản phẩm thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi cập nhật trạng thái sản phẩm: " + e.getMessage());
        }
    }
}
