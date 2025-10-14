package org.example.be_sp.controller;

import org.example.be_sp.model.request.ChiTietPhieuGiamGiaRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.ChiTietPhieuGiamGiaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chi-tiet-phieu-giam-gia-management")
@CrossOrigin(origins = "*")
public class ChiTietPhieuGiamGiaController {
    private static final Logger log = LoggerFactory.getLogger(ChiTietPhieuGiamGiaController.class);

    @Autowired
    ChiTietPhieuGiamGiaService service;

    @GetMapping("/playlist")
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

    @GetMapping("/by-phieu-giam-gia/{idPhieuGiamGia}")
    public ResponseObject<?> getByPhieuGiamGia(@PathVariable Integer idPhieuGiamGia) {
        log.info("Getting product details for coupon: {}", idPhieuGiamGia);
        var result = service.getByIdPhieuGiamGia(idPhieuGiamGia);
        log.info("Found {} product details for coupon {}", result.size(), idPhieuGiamGia);
        return new ResponseObject<>(result);
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody ChiTietPhieuGiamGiaRequest request) {
        service.add(request);
        return new ResponseObject<>(true, null, "Thêm chi tiết phiếu giảm giá thành công");
    }

    @PostMapping("/add-multiple")
    public ResponseObject<?> addMultiple(@RequestParam Integer idPhieuGiamGia, 
                                         @RequestBody List<Integer> idChiTietSanPhams) {
        service.addMultiple(idPhieuGiamGia, idChiTietSanPhams);
        return new ResponseObject<>(true, null, "Thêm sản phẩm vào phiếu giảm giá thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody ChiTietPhieuGiamGiaRequest request) {
        service.update(id, request);
        return new ResponseObject<>(true, null, "Cập nhật chi tiết phiếu giảm giá thành công");
    }

    @PutMapping("/update-by-phieu-giam-gia")
    public ResponseObject<?> updateByPhieuGiamGia(@RequestParam Integer idPhieuGiamGia,
                                                   @RequestBody List<Integer> idChiTietSanPhams) {
        log.info("Updating products for coupon {} with product IDs: {}", idPhieuGiamGia, idChiTietSanPhams);
        service.updateByPhieuGiamGia(idPhieuGiamGia, idChiTietSanPhams);
        log.info("Successfully updated {} products for coupon {}", idChiTietSanPhams.size(), idPhieuGiamGia);
        return new ResponseObject<>(true, null, "Cập nhật sản phẩm áp dụng giảm giá thành công");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable Integer id) {
        service.updateStatus(id);
        return new ResponseObject<>(true, null, "Xoá chi tiết phiếu giảm giá thành công");
    }
}

