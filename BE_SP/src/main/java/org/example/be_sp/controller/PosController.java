package org.example.be_sp.controller;

import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pos")
@CrossOrigin(origins = "*")
public class PosController {

    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;

    @GetMapping("/coupons")
    public ResponseObject<?> getActiveCoupons() {
        try {
            return new ResponseObject<>(true, phieuGiamGiaService.getAll(), "Lấy danh sách phiếu giảm giá thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy danh sách phiếu giảm giá: " + e.getMessage());
        }
    }
}
