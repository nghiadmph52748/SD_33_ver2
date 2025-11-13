package org.example.be_sp.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.be_sp.entity.ChiTietDotGiamGia;
import org.example.be_sp.entity.ChiTietSanPham;
import org.example.be_sp.entity.DiaChiKhachHang;
import org.example.be_sp.entity.HinhThucThanhToan;
import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.HoaDonChiTiet;
import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.entity.PhieuGiamGia;
import org.example.be_sp.entity.PhieuGiamGiaCaNhan;
import org.example.be_sp.entity.TimelineDonHang;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.banHang.ConfirmBanHangRequest;
import org.example.be_sp.model.response.CreateInvoiceResponse;
import org.example.be_sp.repository.ChiTietSanPhamRepository;
import org.example.be_sp.repository.DiaChiKhachHangRepository;
import org.example.be_sp.repository.HinhThucThanhToanRepository;
import org.example.be_sp.repository.HoaDonChiTietRepository;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.repository.NhanVienRepository;
import org.example.be_sp.repository.PhieuGiamGiaCaNhanRepository;
import org.example.be_sp.repository.PhieuGiamGiaRepository;
import org.example.be_sp.repository.PhuongThucThanhToanRepository;
import org.example.be_sp.repository.ThongTinDonHangRepository;
import org.example.be_sp.repository.TimelineDonHangRepository;
import org.example.be_sp.repository.TrangThaiDonHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class BanHangService {

    @Autowired
    ChiTietSanPhamRepository ctspRepository;
    @Autowired
    PhieuGiamGiaRepository pggRepository;
    @Autowired
    PhieuGiamGiaCaNhanRepository pggcnRepository;
    @Autowired
    NhanVienRepository nvRepository;
    @Autowired
    HoaDonChiTietRepository hdctRepository;
    @Autowired
    HoaDonRepository hdRepository;
    @Autowired
    KhachHangRepository khRepository;
    @Autowired
    DiaChiKhachHangRepository dckhRepository;
    @Autowired
    HinhThucThanhToanRepository htttRepository;
    @Autowired
    PhuongThucThanhToanRepository ptttRepository;
    @Autowired
    ThongTinDonHangRepository ttDhRepository;
    @Autowired
    TrangThaiDonHangRepository ttTdRepository;
    @Autowired
    TimelineDonHangRepository timelineRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Helper method to create timeline entry automatically
     */
    private void addTimeline(HoaDon hoaDon, String trangThaiCu, String trangThaiMoi,
            String hanhDong, String moTa, Integer idNhanVien) {
        TimelineDonHang timeline = new TimelineDonHang();
        timeline.setIdHoaDon(hoaDon);
        timeline.setIdNhanVien(nvRepository.findById(idNhanVien).orElseThrow());
        timeline.setTrangThaiCu(trangThaiCu != null ? trangThaiCu : "");
        timeline.setTrangThaiMoi(trangThaiMoi != null ? trangThaiMoi : "");
        timeline.setHanhDong(hanhDong != null ? hanhDong : "");
        timeline.setMoTa(moTa != null ? moTa : "");
        timeline.setThoiGian(java.time.Instant.now());
        timeline.setTrangThai(true);
        timeline.setDeleted(false);
        timelineRepository.save(timeline);
    }

    public Object taoHoaDon(Integer idNhanVien) {
        HoaDon hd = new HoaDon();
        hd.setIdNhanVien(nvRepository.findById(idNhanVien).orElseThrow());
        hd.setCreateAt(LocalDate.now());
        hd.setCreateBy(idNhanVien);
        hd.setGiaoHang(false);
        hd.setGhiChu("Tạo hóa đơn bán hàng tại quầy");
        hd.setTrangThai(true);
        hd.setDeleted(false);
        HoaDon saved = hdRepository.save(hd);

        // Create timeline: Tạo đơn hàng
        addTimeline(saved, "", "Tạo đơn hàng", "Tạo", "Tạo hóa đơn bán hàng tại quầy", idNhanVien);

        // Create timeline: Đang xử lý
        addTimeline(saved, "Tạo đơn hàng", "Đang xử lý", "Cập nhật", "Tạo hóa đơn bán hàng tại quầy", idNhanVien);

        // Call stored procedure to generate invoice code (procedure will UPDATE ma_hoa_don directly)
        String maHoaDon = generateInvoiceCode(saved.getId());

        // Refresh entity to get updated ma_hoa_don from database
        saved = hdRepository.findById(saved.getId()).orElseThrow();

        return new CreateInvoiceResponse(saved.getId(), maHoaDon);
    }

    private String generateInvoiceCode(Integer idHoaDon) {
        try {
            // Call stored procedure with idHoaDon parameter - procedure will UPDATE the column directly
            String sql = "DECLARE @maHoaDon NVARCHAR(10); EXEC sp_GenerateMaHoaDon @idHoaDon = ?, @maMoiGenerated = @maHoaDon OUTPUT; SELECT @maHoaDon as ma_hoa_don";

            String result = jdbcTemplate.queryForObject(sql, new Object[]{idHoaDon}, String.class);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice code: " + e.getMessage(), e);
        }
    }

    public void xoaHoaDon(Integer idHoaDon, Integer idNhanVien) {
        HoaDon hd = hdRepository.findById(idHoaDon).orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với id: " + idHoaDon, "404"));
        hd.setDeleted(true);
        hd.setUpdateAt(LocalDate.now());
        hd.setUpdateBy(idNhanVien);
        hdRepository.save(hd);
        ArrayList<HoaDonChiTiet> lst = hdctRepository.findAllByIdHoaDonAndTrangThai(hd, true);
        Integer[] lstIdHdct = lst.stream().map(HoaDonChiTiet::getId).toArray(Integer[]::new);
        xoaSanPham(lstIdHdct, idNhanVien);

        // Create timeline: Hủy đơn hàng
        addTimeline(hd, "Đang xử lý", "Đã hủy", "Hủy", "Hủy đơn hàng", idNhanVien);
    }

    public Integer themSanPham(Integer idHoaDon, Integer idChiTietSanPham, Integer soLuong, Integer idNhanVien) {
        HoaDon hoaDon = hdRepository.findById(idHoaDon).orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với id: " + idHoaDon, "404"));
        ChiTietSanPham chiTietSanPham = ctspRepository.findById(idChiTietSanPham).orElseThrow(() -> new ApiException("Không tìm thấy chi tiết sản phẩm với id: " + idChiTietSanPham, "404"));
        kiemTra(Map.of(idChiTietSanPham, soLuong), null, idNhanVien, null, null);
        kiemTraTonKhoBienThe(idChiTietSanPham, soLuong);

        // Always create a NEW HoaDonChiTiet instead of updating existing one
        // This ensures each add operation has its own line item that can be independently deleted
        HoaDonChiTiet hdct = new HoaDonChiTiet();
        hdct.setIdHoaDon(hoaDon);
        hdct.setIdChiTietSanPham(chiTietSanPham);
        hdct.setSoLuong(soLuong);
        hdct.setGiaBan(chiTietSanPham.getGiaBan().multiply(BigDecimal.valueOf(chiTietSanPham.getChiTietDotGiamGias().stream().filter(ChiTietDotGiamGia::getTrangThai).mapToDouble(ctdg -> (100 - ctdg.getIdDotGiamGia().getGiaTriGiamGia()) / 100.0).reduce(1.0, (a, b) -> a * b))));
        hdct.setThanhTien(chiTietSanPham.getGiaBan().multiply(BigDecimal.valueOf(soLuong)));
        hdct.setTrangThai(true);
        hdct.setDeleted(false);
        hdct.setCreateAt(LocalDate.now());
        hdct.setCreateBy(idNhanVien);
        HoaDonChiTiet saved = hdctRepository.save(hdct);
        ChiTietSanPham ctsp = ctspRepository.findById(idChiTietSanPham).orElseThrow();
        ctsp.setSoLuong(ctsp.getSoLuong() - soLuong);
        ctspRepository.save(ctsp);

        // Cập nhật tổng tiền hóa đơn
        updateTongTienHoaDon(hoaDon);

        // Create timeline: Thêm sản phẩm
        addTimeline(hoaDon, "Đang xử lý", "Đang xử lý", "Thêm",
                "Thêm sản phẩm (ID: " + idChiTietSanPham + ", SL: " + soLuong + ")",
                idNhanVien);

        return saved.getId();
    }

    public void xoaSanPham(Integer[] idChiTietSanPham, Integer idNhanVien) {
        HoaDon hoaDon = null;
        for (Integer idCtsp : idChiTietSanPham) {
            HoaDonChiTiet hdct = hdctRepository.findById(idCtsp).orElseThrow(() -> new ApiException("Không tìm thấy chi tiết hóa đơn với id: " + idCtsp, "404"));

            ChiTietSanPham ctsp = ctspRepository.findById(hdct.getIdChiTietSanPham().getId()).orElseThrow();

            hdct.setDeleted(true);
            hdct.setUpdateAt(LocalDate.now());
            hdct.setUpdateBy(idNhanVien);
            hdctRepository.save(hdct);

            ctsp.setSoLuong(ctsp.getSoLuong() + hdct.getSoLuong());
            ctspRepository.save(ctsp);

            // Keep track of hoaDon to update tongTien later
            if (hoaDon == null) {
                hoaDon = hdct.getIdHoaDon();
            }
        }

        // Update hoaDon tongTien after deleting all items
        if (hoaDon != null) {
            updateTongTienHoaDon(hoaDon);

            // Create timeline: Xóa sản phẩm
            addTimeline(hoaDon, "Đang xử lý", "Đang xử lý", "Xóa",
                    "Xóa " + idChiTietSanPham.length + " sản phẩm khỏi đơn hàng",
                    idNhanVien);
        }
    }    // Helper method to calculate and update total amount (tongTien) of HoaDon

    private void updateTongTienHoaDon(HoaDon hoaDon) {
        ArrayList<HoaDonChiTiet> chiTiets = hdctRepository.findAllByIdHoaDonAndTrangThai(hoaDon, true);
        if (chiTiets != null && !chiTiets.isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (HoaDonChiTiet ct : chiTiets) {
                totalAmount = totalAmount.add(ct.getThanhTien() != null ? ct.getThanhTien() : BigDecimal.ZERO);
            }
            hoaDon.setTongTien(totalAmount);
            hdRepository.save(hoaDon);
        }
    }

    public void updateSoLuongSanPham(Integer idHoaDonChiTiet, Integer soLuong, Integer idNhanVien) {
        HoaDonChiTiet hdct = hdctRepository.findById(idHoaDonChiTiet)
                .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết hóa đơn với id: " + idHoaDonChiTiet, "404"));

        // Lấy số lượng cũ và tính diff
        Integer oldQuantity = hdct.getSoLuong();
        Integer diff = soLuong - oldQuantity;

        // Nếu tăng số lượng, kiểm tra tồn kho trước khi update
        if (diff > 0) {
            ChiTietSanPham ctsp = ctspRepository.findById(hdct.getIdChiTietSanPham().getId()).orElseThrow();
            // Check nếu tồn kho không đủ
            if (ctsp.getSoLuong() < diff) {
                throw new ApiException("Tồn kho không đủ! Yêu cầu tăng: " + diff + " | Còn lại: " + ctsp.getSoLuong(), "400");
            }
        }

        // Cập nhật số lượng trong chi tiết hóa đơn
        hdct.setSoLuong(soLuong);
        hdct.setThanhTien(hdct.getGiaBan().multiply(BigDecimal.valueOf(soLuong)));
        hdct.setUpdateAt(LocalDate.now());
        hdct.setUpdateBy(idNhanVien);
        hdctRepository.save(hdct);

        // Cập nhật tồn kho chi tiết sản phẩm (chỉ update sau khi HoaDonChiTiet đã save)
        ChiTietSanPham ctsp = ctspRepository.findById(hdct.getIdChiTietSanPham().getId()).orElseThrow();
        Integer newStock = ctsp.getSoLuong() - diff;

        // Double check: tồn kho không được âm
        if (newStock < 0) {
            throw new ApiException("Lỗi: tồn kho sẽ âm! Điều này không nên xảy ra. Liên hệ admin.", "500");
        }

        ctsp.setSoLuong(newStock);
        ctspRepository.save(ctsp);

        // Create timeline: Cập nhật số lượng
        addTimeline(hdct.getIdHoaDon(), "Đang xử lý", "Đang xử lý", "Cập nhật",
                "Cập nhật số lượng: " + oldQuantity + " → " + soLuong,
                idNhanVien);
    }

    public Integer updateKhachHang(Integer idHoaDon, Integer idKhachHang, String tenKhachHang, String soDienThoai, String diaChiKhachHang, String emailKhachHang, Integer idNhanVien) {
        HoaDon hoaDon = hdRepository.findById(idHoaDon).orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với id: " + idHoaDon, "404"));

        if (idKhachHang != null) {
            // Update with registered customer
            KhachHang khachHang = khRepository.findById(idKhachHang).orElseThrow(() -> new ApiException("Không tìm thấy khách hàng với id: " + idKhachHang, "404"));
            hoaDon.setIdKhachHang(khachHang);
            if (tenKhachHang != null) {
                hoaDon.setTenNguoiNhan(tenKhachHang);
            } else {
                hoaDon.setTenNguoiNhan(khachHang.getTenKhachHang());
            }
            if (soDienThoai != null) {
                hoaDon.setSoDienThoaiNguoiNhan(soDienThoai);
            } else {
                hoaDon.setSoDienThoaiNguoiNhan(khachHang.getSoDienThoai());
            }
            if (emailKhachHang != null) {
                hoaDon.setEmailNguoiNhan(emailKhachHang);
            } else {
                hoaDon.setEmailNguoiNhan(khachHang.getEmail());
            }
            if (diaChiKhachHang != null) {
                hoaDon.setDiaChiNguoiNhan(diaChiKhachHang);
            } else {
                ArrayList<DiaChiKhachHang> dckh = dckhRepository.findAllByIdKhachHangAndTrangThaiAndDeleted(khachHang, true, false);
                if (dckh != null) {
                    for (DiaChiKhachHang dc : dckh) {
                        if (dc.getMacDinh()) {
                            hoaDon.setDiaChiNguoiNhan(dc.getDiaChi());
                            break;
                        }
                    }
                } else {
                    hoaDon.setDiaChiNguoiNhan(null);
                }
            }
            hoaDon.setUpdateAt(LocalDate.now());
            hoaDon.setUpdateBy(idNhanVien);
            hdRepository.save(hoaDon);

            // Create timeline: Cập nhật khách hàng
            addTimeline(hoaDon, "Đang xử lý", "Đang xử lý", "Cập nhật",
                    "Cập nhật khách hàng: " + (tenKhachHang != null ? tenKhachHang : khachHang.getTenKhachHang()),
                    idNhanVien);

            return khachHang.getId();
        } else {
            // Switch to walk-in customer (khách lẻ) - clear customer info
            hoaDon.setIdKhachHang(null);
            hoaDon.setTenNguoiNhan(tenKhachHang != null ? tenKhachHang : "Khách lẻ");
            hoaDon.setSoDienThoaiNguoiNhan(soDienThoai);
            hoaDon.setDiaChiNguoiNhan(diaChiKhachHang);
            hoaDon.setEmailNguoiNhan(emailKhachHang);
            hoaDon.setUpdateAt(LocalDate.now());
            hoaDon.setUpdateBy(idNhanVien);
            hdRepository.save(hoaDon);

            // Create timeline: Chuyển sang khách lẻ
            addTimeline(hoaDon, "Đang xử lý", "Đang xử lý", "Cập nhật",
                    "Chuyển sang khách lẻ",
                    idNhanVien);

            return null;
        }
    }

    public void updateGiaoHang(Integer idHoaDon, Integer idNhanVien) {
        HoaDon hoaDon = hdRepository.findById(idHoaDon).orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với id: " + idHoaDon, "404"));
        boolean giaohangCu = hoaDon.getGiaoHang();
        hoaDon.setGiaoHang(!hoaDon.getGiaoHang());
        if (hoaDon.getGiaoHang()) {
            hoaDon.setPhiVanChuyen(BigDecimal.valueOf(30000));
        }
        hoaDon.setUpdateAt(LocalDate.now());
        hoaDon.setUpdateBy(idNhanVien);
        hdRepository.save(hoaDon);

        // Create timeline: Cập nhật hình thức giao hàng
        addTimeline(hoaDon, "Đang xử lý", "Đang xử lý", "Cập nhật",
                "Cập nhật hình thức giao hàng: " + (giaohangCu ? "Không giao" : "Giao hàng")
                + " → " + (hoaDon.getGiaoHang() ? "Giao hàng" : "Không giao"),
                idNhanVien);
    }

    public void updateHTTT(Integer idHoaDon, Integer idPTTT, Integer idNhanVien) {
        HoaDon hoaDon = hdRepository.findById(idHoaDon).orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với id: " + idHoaDon, "404"));
        HinhThucThanhToan httt
                = htttRepository.findByIdHoaDonAndTrangThaiAndDeleted(hdRepository.findById(idHoaDon).orElseThrow(),
                        true,
                        false);

        // Create new HinhThucThanhToan if none exists
        if (httt == null) {
            httt = new HinhThucThanhToan();
        }

        httt.setIdHoaDon(hoaDon);
        httt.setIdPhuongThucThanhToan(ptttRepository.findById(idPTTT).orElseThrow());
        httt.setTrangThai(true);
        httt.setDeleted(false);
        htttRepository.save(httt);
        hoaDon.setUpdateAt(LocalDate.now());
        hoaDon.setUpdateBy(idNhanVien);
        hdRepository.save(hoaDon);

        // Create timeline: Cập nhật hình thức thanh toán
        String ptttName = idPTTT == 1 ? "Tiền mặt" : idPTTT == 2 ? "Chuyển khoản" : "Cả hai";
        addTimeline(hoaDon, "Đang xử lý", "Đang xử lý", "Cập nhật",
                "Cập nhật hình thức thanh toán: " + ptttName,
                idNhanVien);
    }

    public Integer updatePhieuGiamGia(Integer idHoaDon, Integer idPhieuGiamGia, Integer idNhanVien) {
        HoaDon hoaDon = hdRepository.findById(idHoaDon).orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với id: " + idHoaDon, "404"));
        PhieuGiamGia pgg = pggRepository.findById(idPhieuGiamGia).orElseThrow(() -> new ApiException("Không tìm thấy phiếu giảm giá với id: " + idPhieuGiamGia, "404"));
        kiemTra(Map.of(), idPhieuGiamGia, idNhanVien, null, null);

        // Check if tongTien is null (invoice with no items yet)
        if (hoaDon.getTongTien() == null || hoaDon.getTongTien().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Hóa đơn chưa có sản phẩm nào, không thể áp dụng voucher", "400");
        }

        hoaDon.setIdPhieuGiamGia(pgg);
        if (pgg.getLoaiPhieuGiamGia()) {
            hoaDon.setTongTienSauGiam(hoaDon.getTongTien().subtract(pgg.getGiaTriGiamGia()));
        } else {
            hoaDon.setTongTienSauGiam(hoaDon.getTongTien().multiply(BigDecimal.valueOf((BigDecimal.valueOf(100).subtract(pgg.getGiaTriGiamGia()).doubleValue()) / 100.0)));
        }
        hoaDon.setUpdateAt(LocalDate.now());
        hoaDon.setUpdateBy(idNhanVien);
        hdRepository.save(hoaDon);

        // Create timeline: Áp dụng phiếu giảm giá
        addTimeline(hoaDon, "Đang xử lý", "Đang xử lý", "Cập nhật",
                "Áp dụng phiếu giảm giá: " + pgg.getMaPhieuGiamGia(),
                idNhanVien);

        return pgg.getId();
    }

    public void banHangTaiQuay(ConfirmBanHangRequest request) {
        // Get all HoaDonChiTiet items for validation
        HoaDon hoaDon = hdRepository.findById(request.getIdHoaDon()).orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với id: " + request.getIdHoaDon(), "404"));
        ArrayList<HoaDonChiTiet> chiTiets = hdctRepository.findAllByIdHoaDonAndTrangThai(hoaDon, true);

        // Build map of products for validation
        Map<Integer, Integer> danhSachSanPham = new HashMap<>();
        if (chiTiets != null && !chiTiets.isEmpty()) {
            for (HoaDonChiTiet ct : chiTiets) {
                danhSachSanPham.put(ct.getIdChiTietSanPham().getId(), ct.getSoLuong());
            }
        }

        kiemTra(danhSachSanPham, request.getIdPhieuGiamGia(), request.getIdNhanVien(), request.getIdKhachHang(), request.getIdPTTT());
        hoaDon.setId(request.getIdHoaDon());
        hoaDon.setTrangThaiThanhToan(request.getTrangThaiThanhToan());
        // Default tienMat and tienChuyenKhoan to 0 if null
        BigDecimal tienMat = request.getTienMat() != null ? request.getTienMat() : BigDecimal.ZERO;
        BigDecimal tienChuyenKhoan = request.getTienChuyenKhoan() != null ? request.getTienChuyenKhoan() : BigDecimal.ZERO;
        hoaDon.setSoTienDaThanhToan(tienMat.add(tienChuyenKhoan));
        hoaDon.setSoTienConLai(request.getSoTienConLai());
        hoaDon.setGhiChu("Bán hàng tại quầy");
        hdRepository.save(hoaDon);

        // Handle voucher (only process if provided)
        if (request.getIdPhieuGiamGia() != null) {
            PhieuGiamGia pgg = pggRepository.findById(request.getIdPhieuGiamGia()).orElseThrow();

            // Handle personal vouchers (featured = true)
            if (pgg.getFeatured() && request.getIdKhachHang() != null) {
                // Mark personal voucher as used (trangThai = false) so customer can't use it again
                PhieuGiamGiaCaNhan pggcn = pggcnRepository.findByIdKhachHangAndIdPhieuGiamGiaAndTrangThaiAndDeleted(
                        khRepository.findById(request.getIdKhachHang()).orElseThrow(),
                        pggRepository.findById(request.getIdPhieuGiamGia()).orElseThrow(),
                        true, // Looking for unused voucher (trangThai = true)
                        false
                );
                if (pggcn != null) {
                    // Mark as used by setting trangThai = false
                    pggcn.setTrangThai(false);
                    pggcnRepository.save(pggcn);
                } else {
                    // This shouldn't happen if validation is correct, but log for debugging
                    throw new ApiException("Phiếu giảm giá cá nhân này không tìm thấy hoặc đã được sử dụng", "400");
                }
                // NOTE: Personal vouchers don't decrease shared soLuongDung
                // Each customer has their own quantity based on PhieuGiamGiaCaNhan
            } else {
                // Handle public vouchers (featured = false)
                // Decrease the shared quantity counter for public vouchers
                pgg.setSoLuongDung(pgg.getSoLuongDung() - 1);
                pggRepository.save(pgg);
            }
        }
        // Note: idPTTT (1: cash, 2: transfer, 3: both) is just recorded in hoaDon
        // No need to update HinhThucThanhToan entity for POS orders

        // Create timeline: Xác nhận bán hàng
        addTimeline(hoaDon, "Đang xử lý", "Hoàn thành", "Xác nhận",
                "Xác nhận bán hàng tại quầy - Tổng tiền: " + hoaDon.getTongTien(),
                request.getIdNhanVien());
    }

    public void kiemTraTonKhoBienThe(Integer idChiTietSanPham, Integer soLuong) {
        Integer tonKhoHienTai = ctspRepository.findById(idChiTietSanPham).orElseThrow().getSoLuong();
        if (tonKhoHienTai < soLuong) {
            throw new ApiException("Chi tiết sản phẩm với id: " + idChiTietSanPham + " không đủ tồn kho", "400");
        }
    }

    public Boolean kiemTraTonKhoPhieuGiamGia(Integer idPhieuGiamGia, Integer idKhachHang) {
        if (idPhieuGiamGia == null) {
            return true;
        }

        PhieuGiamGia pgg = pggRepository.findById(idPhieuGiamGia).orElseThrow();
        Integer soLuongTonKho = pgg.getSoLuongDung();

        // Check public voucher quantity
        if (soLuongTonKho == null || soLuongTonKho <= 0) {
            return false;
        }

        // If personal voucher (featured=true) and customer is specified, check customer-specific status
        if (pgg.getFeatured() && idKhachHang != null) {
            KhachHang khachHang = khRepository.findById(idKhachHang).orElseThrow();
            PhieuGiamGiaCaNhan pggcn = pggcnRepository.findByIdKhachHangAndIdPhieuGiamGiaAndTrangThaiAndDeleted(
                    khachHang, pgg, true, false
            );
            // If no record found or trangThai=false, customer cannot use this voucher
            if (pggcn == null || !pggcn.getTrangThai()) {
                return false;
            }
        }

        return true;
    }

    public Boolean kiemTraTonKhoPhieuGiamGia(Integer idPhieuGiamGia) {
        // Backward compatibility without customer check
        return kiemTraTonKhoPhieuGiamGia(idPhieuGiamGia, null);
    }

    public void kiemTra(Map<Integer, Integer> listIdChiTietSanPham, Integer idPhieuGiamGia, Integer idNhanVien, Integer idKhachHang, Integer idPhuongThucThanhToan) {
        listIdChiTietSanPham.forEach((id, soLuong) -> {
            Boolean ctspTT = ctspRepository.findById(id).orElseThrow(() -> new ApiException("Không tìm thấy chi tiết sản phẩm với id: " + id, "404")).getTrangThai();
            if (!ctspTT) {
                throw new ApiException("Chi tiết sản phẩm với id: " + id + " không hoạt động", "400");
            }
        });
        if (idPhieuGiamGia != null) {
            Boolean pggTT = pggRepository.findById(idPhieuGiamGia).orElseThrow(() -> new ApiException("Không tìm thấy phiếu giảm giá với id: " + idPhieuGiamGia, "404")).getTrangThai();
            if (!pggTT) {
                throw new ApiException("Phiếu giảm giá với id: " + idPhieuGiamGia + " không hoạt động", "400");
            }
            // Check stock and personal voucher status for specific customer
            Boolean result = kiemTraTonKhoPhieuGiamGia(idPhieuGiamGia, idKhachHang);
            if (!result) {
                throw new ApiException("Phiếu giảm giá với id: " + idPhieuGiamGia + " không đủ tồn kho hoặc khách hàng không được phép sử dụng", "400");
            }
        }
        Boolean nvTT = nvRepository.findById(idNhanVien).orElseThrow(() -> new ApiException("Không tìm thấy nhân viên với id: " + idNhanVien, "404")).getTrangThai();
        if (!nvTT) {
            throw new ApiException("Nhân viên với id: " + idNhanVien + " không hoạt động", "400");
        }
        if (idKhachHang != null) {
            Boolean khTT = khRepository.findById(idKhachHang).orElseThrow(() -> new ApiException("Không tìm thấy khách hàng với id: " + idKhachHang, "404")).getTrangThai();
            if (!khTT) {
                throw new ApiException("Khách hàng với id: " + idKhachHang + " không hoạt động", "400");
            }
        }
        // Note: idPhuongThucThanhToan is actually idPTTT (1:cash, 2:transfer, 3:both) - not a database ID
        // So we don't validate it here
    }

    public ArrayList<TimelineDonHang> getTimelineByHoaDon(Integer idHoaDon) {
        // Verify hóa đơn exists before fetching timeline
        hdRepository.findById(idHoaDon).orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với id: " + idHoaDon, "404"));
        List<TimelineDonHang> timeline = timelineRepository.findByHoaDonId(idHoaDon);
        return timeline != null ? new ArrayList<>(timeline) : new ArrayList<>();
    }
}
