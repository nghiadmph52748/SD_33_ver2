package org.example.be_sp.controller;

import org.example.be_sp.model.request.banHang.ConfirmBanHangRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.BanHangService;
import org.example.be_sp.service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pos")
@CrossOrigin(origins = "*")
public class PosController {

    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;
    @Autowired
    private BanHangService banHangService;

    @GetMapping("/get/hoa-don-cho")
    public ResponseObject<?> getHoaDonCho() {
        try {
            return new ResponseObject<>(true, banHangService.getHoaDonCho(), "Lấy hóa đơn chờ thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy hóa đơn chờ: " + e.getMessage());
        }
    }

    @GetMapping("/coupons")
    public ResponseObject<?> getActiveCoupons() {
        try {
            return new ResponseObject<>(true, phieuGiamGiaService.getPosActiveCoupons(), "Lấy danh sách phiếu giảm giá thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy danh sách phiếu giảm giá: " + e.getMessage());
        }
    }

    @GetMapping("/coupons/for-customer/{idKhachHang}")
    public ResponseObject<?> getActiveCouponsForCustomer(@PathVariable Integer idKhachHang) {
        try {
            return new ResponseObject<>(true, phieuGiamGiaService.getCouponsForCustomer(idKhachHang), "Lấy danh sách phiếu giảm giá cho khách hàng thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy danh sách phiếu giảm giá: " + e.getMessage());
        }
    }

    @PostMapping("/create-invoice")
    public ResponseObject<?> taoHoaDon(@RequestParam("createBy") Integer idNhanVien) {
        try {
            Object response = banHangService.taoHoaDon(idNhanVien);
            return new ResponseObject<>(true, response, "Tạo hóa đơn thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi tạo hóa đơn: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-invoice")
    public ResponseEntity<?> deleteHoaDonChiTiet(@RequestParam Integer idHoaDon, @RequestParam Integer idNhanVien) {
        try {
            banHangService.xoaHoaDon(idHoaDon, idNhanVien);
            return ResponseEntity.ok(new ResponseObject<>(true, null, "Xóa hóa đơn thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject<>(false, null, "Lỗi khi xóa hóa đơn: " + e.getMessage()));
        }

    }

    @PostMapping("/add-product-to-invoice-detail")
    public ResponseObject<?> addSanPhamVaoHoaDonChiTiet(@RequestParam Integer idHoaDon,
            @RequestParam Integer idChiTietSanPham,
            @RequestParam Integer soLuong, @RequestParam Integer idNhanVien) {
        try {
            Integer invoiceDetailId = banHangService.themSanPham(idHoaDon, idChiTietSanPham, soLuong, idNhanVien);
            return new ResponseObject<>(true, invoiceDetailId, "Thêm sản phẩm vào hóa đơn thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi thêm sản phẩm vào hóa đơn: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteBienTheTrongGioHang(@RequestParam Integer[] idHoaDonChiTiet,
            @RequestParam Integer idNhanVien) {
        try {
            banHangService.xoaSanPham(idHoaDonChiTiet, idNhanVien);
            return ResponseEntity.ok(new ResponseObject<>(true, null, "Xóa sản phẩm khỏi hóa đơn thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject<>(false, null, "Lỗi khi xóa sản phẩm khỏi hóa đơn: " + e.getMessage()));
        }
    }

    @PutMapping("/update-product-quantity")
    public ResponseObject<?> updateSoLuongSanPham(@RequestParam Integer idHoaDonChiTiet,
            @RequestParam Integer soLuong,
            @RequestParam Integer idNhanVien) {
        try {
            banHangService.updateSoLuongSanPham(idHoaDonChiTiet, soLuong, idNhanVien);
            return new ResponseObject<>(true, null, "Cập nhật số lượng sản phẩm thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi cập nhật số lượng sản phẩm: " + e.getMessage());
        }
    }

    @PutMapping("/update-customer")
    public ResponseObject<?> updateKhachHang(@RequestBody ConfirmBanHangRequest request) {
        try {
            Integer idKhachHang = banHangService.updateKhachHang(request.getIdHoaDon(), request.getIdKhachHang(),
                    request.getTenKhachHang(), request.getSoDienThoai(), request.getDiaChiKhachHang(),
                    request.getEmailKhachHang(), request.getIdNhanVien());
            return new ResponseObject<>(true, idKhachHang, "Cập nhật thông tin khách hàng thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi cập nhật thông tin khách hàng: " + e.getMessage());
        }
    }

    @PutMapping("/update-giao-hang")
    public ResponseObject<?> updateGiaoHang(@RequestParam Integer idHoaDon, @RequestParam Boolean loaiDon,
            @RequestParam(required = false) java.math.BigDecimal phiVanChuyen, @RequestParam Integer idNhanVien) {
        try {
            banHangService.updateGiaoHang(idHoaDon, loaiDon, phiVanChuyen, idNhanVien);
            return new ResponseObject<>(true, null, "Cập nhật hình thức giao hàng thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi cập nhật hình thức giao hàng: " + e.getMessage());
        }
    }

    @PutMapping("/update-payment-method")
    public ResponseObject<?> updateHinhThucThanhToan(@RequestBody ConfirmBanHangRequest request) {
        try {
            banHangService.updateHTTT(request.getIdHoaDon(), request.getIdPTTT(), request.getIdNhanVien());
            return new ResponseObject<>(true, null, "Cập nhật hình thức thanh toán thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi cập nhật hình thức thanh toán: " + e.getMessage());
        }
    }

    @PutMapping("/update-voucher")
    public ResponseObject<?> updateVoucher(@RequestBody ConfirmBanHangRequest request) {
        try {
            Integer idVoucher = banHangService.updatePhieuGiamGia(request.getIdHoaDon(), request.getIdPhieuGiamGia(),
                    request.getIdNhanVien());
            return new ResponseObject<>(true, idVoucher, "Cập nhật phiếu giảm giá thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi cập nhật phiếu giảm giá: " + e.getMessage());
        }
    }

    @PostMapping("/confirm")
    public ResponseObject<?> xacNhanToanBoDonHang(@RequestBody ConfirmBanHangRequest request) {
        try {
            banHangService.banHangTaiQuay(request);
            return new ResponseObject<>(true, null, "Bán hàng thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi bán hàng: " + e.getMessage());
        }
    }

    @GetMapping("/timeline/{idHoaDon}")
    public ResponseObject<?> getInvoiceTimeline(@PathVariable Integer idHoaDon) {
        try {
            return new ResponseObject<>(true, banHangService.getTimelineByHoaDon(idHoaDon), "Lấy timeline đơn hàng thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi lấy timeline: " + e.getMessage());
        }
    }

    @GetMapping("/validate-invoice/{idHoaDon}")
    public ResponseObject<?> validateInvoiceBeforeConfirm(@PathVariable Integer idHoaDon) {
        try {
            return new ResponseObject<>(true, banHangService.validateInvoiceBeforeConfirm(idHoaDon), "Kiểm tra hóa đơn thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Lỗi khi kiểm tra hóa đơn: " + e.getMessage());
        }
    }
}
