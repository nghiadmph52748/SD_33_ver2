package org.example.be_sp.controller;

import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.ThongTinHoaDonService;
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
@RequestMapping("/api/thong-tin-hoa-don-management")
@CrossOrigin(origins = "*")
public class ThongTinHoaDonController {
    @Autowired
    private ThongTinHoaDonService thongTinHoaDonService;
    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(thongTinHoaDonService.getAll(), "Hien thi thanh cong");
    }
    @GetMapping("/paging")
    public ResponseObject<?> phanTrang(@RequestParam(defaultValue = "0") Integer no,@RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(thongTinHoaDonService.phanTrang(no, size), "Phan trang thanh cong");
    }
    @GetMapping("/detail/{id}")
    public ResponseObject<?> getById(@PathVariable Integer id) {
        return new ResponseObject<>(thongTinHoaDonService.getById(id), "Hien thi chi tiet thanh cong");
    }
    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody org.example.be_sp.model.request.ThongTinHoaDonRequest thongTinHoaDonRequest) {
        thongTinHoaDonService.add(thongTinHoaDonRequest);
        return new ResponseObject<>(null, "Them moi thanh cong");
    }
    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody org.example.be_sp.model.request.ThongTinHoaDonRequest thongTinHoaDonRequest) {
        thongTinHoaDonService.update(id, thongTinHoaDonRequest);
        return new ResponseObject<>(null, "Cap nhat thanh cong");
    }
    @PutMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable Integer id) {
        thongTinHoaDonService.delete(id);
        return new ResponseObject<>(null, "Xoa thanh cong");
    }
    
    @GetMapping("/by-hoa-don/{hoaDonId}")
    public ResponseObject<?> getByHoaDonId(@PathVariable Integer hoaDonId) {
        return new ResponseObject<>(thongTinHoaDonService.getByHoaDonId(hoaDonId), "Lấy thông tin đơn hàng theo hóa đơn thành công");
    }
    
    @GetMapping("/latest-by-hoa-don/{hoaDonId}")
    public ResponseObject<?> getLatestByHoaDonId(@PathVariable Integer hoaDonId) {
        return new ResponseObject<>(thongTinHoaDonService.getLatestByHoaDonId(hoaDonId), "Lấy thông tin đơn hàng mới nhất thành công");
    }
    
    @GetMapping("/san-pham-da-ban/{hoaDonId}")
    public ResponseObject<?> getSanPhamDaBan(@PathVariable Integer hoaDonId) {
        return new ResponseObject<>(thongTinHoaDonService.getSanPhamDaBanByHoaDonId(hoaDonId), "Lấy danh sách sản phẩm đã bán thành công");
    }
}
