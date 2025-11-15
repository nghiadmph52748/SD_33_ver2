package org.example.be_sp.controller;

import org.example.be_sp.model.request.AddProductToCartRequest;
import org.example.be_sp.model.request.HoaDonChiTietRequest;
import org.example.be_sp.model.response.AddProductToCartResponse;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.HoaDonChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hoa-don-chi-tiet-management")
@CrossOrigin(origins = "*")
public class HoaDonChiTietController {

    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(hoaDonChiTietService.getAll(), "Hiển thị thành công");
    }

    @GetMapping("/paging")
    public ResponseObject<?> getAllPaging(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(hoaDonChiTietService.phanTrang(page, size), "Hiển thị thành công");
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> getById(@PathVariable Integer id) {
        return new ResponseObject<>(hoaDonChiTietService.getById(id), "Hiển thị thành công id: " + id);
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody HoaDonChiTietRequest hoaDonChiTietRequest) {
        hoaDonChiTietService.add(hoaDonChiTietRequest);
        return new ResponseObject<>(true, null, "Thêm mới thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody HoaDonChiTietRequest hoaDonChiTietRequest) {
        hoaDonChiTietService.update(id, hoaDonChiTietRequest);
        return new ResponseObject<>(true, null, "Cập nhật thành công id: " + id);
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        hoaDonChiTietService.updateTrangThai(id);
        return new ResponseObject<>(true, null, "Xóa hóa đơn chi tiết thành công id: " + id);
    }

    @PostMapping("/add-to-cart")
    public ResponseObject<?> addToCart(@RequestBody AddProductToCartRequest request) {
        AddProductToCartResponse response = hoaDonChiTietService.addProductToCart(request);
        return ResponseObject.success(response, "Thêm sản phẩm vào giỏ hàng thành công");
    }
}
