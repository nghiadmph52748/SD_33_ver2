package org.example.be_sp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.be_sp.entity.ChiTietSanPham;
import org.example.be_sp.entity.HinhThucThanhToan;
import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.HoaDonChiTiet;
import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.entity.PhieuGiamGia;
import org.example.be_sp.entity.PhieuGiamGiaCaNhan;
import org.example.be_sp.entity.PhuongThucThanhToan;
import org.example.be_sp.entity.ThongTinDonHang;
import org.example.be_sp.entity.TimelineDonHang;
import org.example.be_sp.entity.TrangThaiDonHang;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.email.OrderEmailData;
import org.example.be_sp.model.email.RefundNotificationEmailData;
import org.example.be_sp.model.email.VoucherEmailData;
import org.example.be_sp.model.request.AddressChangeNotificationRequest;
import org.example.be_sp.model.request.BanHangTaiQuayRequest;
import org.example.be_sp.model.request.HoaDonChiTietRequest;
import org.example.be_sp.model.response.HoaDonResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.ChiTietSanPhamRepository;
import org.example.be_sp.repository.HinhThucThanhToanRepository;
import org.example.be_sp.repository.HoaDonChiTietRepository;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.repository.NhanVienRepository;
import org.example.be_sp.repository.PhuongThucThanhToanRepository;
import org.example.be_sp.repository.ThongTinDonHangRepository;
import org.example.be_sp.repository.TimelineDonHangRepository;
import org.example.be_sp.repository.TrangThaiDonHangRepository;
import org.example.be_sp.repository.PhieuGiamGiaRepository;
import org.example.be_sp.repository.PhieuGiamGiaCaNhanRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class HoaDonService {

    private static final BigDecimal REFUND_VOUCHER_THRESHOLD = new BigDecimal("50000");

    @Autowired
    private HoaDonRepository hoaDonRepository;
    @Autowired
    KhachHangRepository khachHangRepository;
    @Autowired
    NhanVienRepository nhanVienRepository;
    @Autowired
    ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired
    HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired
    PhieuGiamGiaService phieuGiamGiaService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ThongTinDonHangRepository thongTinDonHangRepository;
    @Autowired
    private TrangThaiDonHangRepository trangThaiDonHangRepository;
    @Autowired
    private TimelineDonHangRepository timelineDonHangRepository;
    @Autowired
    private HinhThucThanhToanRepository hinhThucThanhToanRepository;
    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    private PhieuGiamGiaCaNhanRepository phieuGiamGiaCaNhanRepository;

    public List<HoaDonResponse> getAll() {
        return hoaDonRepository.findAll().stream().map(HoaDonResponse::new).toList();
    }

    public PagingResponse<HoaDonResponse> phanTrang(Integer no, Integer size) {
        Pageable page = PageRequest.of(no, size);
        return new PagingResponse<>(hoaDonRepository.findAll(page).map(HoaDonResponse::new), no);
    }

    public HoaDonResponse getByid(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn", "404"));
        return new HoaDonResponse(hoaDon);
    }

    public HoaDonResponse getByMaHoaDon(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            throw new ApiException("Mã hóa đơn không hợp lệ", "400");
        }
        HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(maHoaDon.trim())
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn", "404"));
        return new HoaDonResponse(hoaDon);
    }

    public HoaDonResponse add(BanHangTaiQuayRequest request) {
        HoaDon hd = MapperUtils.map(request, HoaDon.class);
        if (request.getIdKhachHang() != null) {
            hd.setIdKhachHang(khachHangRepository.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new ApiException("Khách hàng không tồn tại", "404")));
        }
        if (request.getIdPhieuGiamGia() != null) {
            hd.setIdPhieuGiamGia(phieuGiamGiaService.getById(request.getIdPhieuGiamGia()));
        }
        if (request.getIdNhanVien() != null) {
            hd.setIdNhanVien(nhanVienRepository.findById(request.getIdNhanVien())
                    .orElseThrow(() -> new ApiException("Nhân viên không tồn tại", "404")));
        }
        // Tự động điền tên và mã nhân viên
        if (hd.getIdNhanVien() != null) {
            if (request.getTenNhanVien() == null || request.getTenNhanVien().trim().isEmpty()) {
                hd.setTenNhanVien(hd.getIdNhanVien().getTenNhanVien());
            }
            if (request.getMaNhanVien() == null || request.getMaNhanVien().trim().isEmpty()) {
                hd.setMaNhanVien(hd.getIdNhanVien().getMaNhanVien());
            }
        }
        // Tự động điền tên phiếu giảm giá (nhưng KHÔNG điền mã để tránh truncation)
        if (hd.getIdPhieuGiamGia() != null) {
            if (request.getTenPhieuGiamGia() == null || request.getTenPhieuGiamGia().trim().isEmpty()) {
                hd.setTenPhieuGiamGia(hd.getIdPhieuGiamGia().getTenPhieuGiamGia());
            }
            // Do NOT auto-populate maPhieuGiamGia - keep it NULL to avoid DB column
            // truncation
            // hd.setMaPhieuGiamGia(hd.getIdPhieuGiamGia().getMaPhieuGiamGia());
        }
        // Map diaChiNhanHang từ request vào diaChiNguoiNhan của entity
        // (vì tên field khác nhau nên ModelMapper không tự động map)
        if (request.getDiaChiNhanHang() != null && !request.getDiaChiNhanHang().trim().isEmpty()) {
            hd.setDiaChiNguoiNhan(request.getDiaChiNhanHang());
        }
        // Set ngayTao if not provided
        if (hd.getNgayTao() == null) {
            hd.setNgayTao(LocalDateTime.now());
        }
        // Set createAt if not provided
        if (hd.getCreateAt() == null) {
            hd.setCreateAt(LocalDateTime.now());
        }
        if (request.getLoaiDon() != null) {
            hd.setGiaoHang(request.getLoaiDon()); // Default to true (online order)
        }
        HoaDon savedHoaDon = hoaDonRepository.save(hd);
        // Create HoaDonChiTiet from request.hoaDonChiTiet (for orders from banHangMain)
        if (request.getHoaDonChiTiet() != null && !request.getHoaDonChiTiet().isEmpty()) {
            try {
                for (HoaDonChiTietRequest chiTietRequest : request.getHoaDonChiTiet()) {
                    HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
                    hoaDonChiTiet.setIdHoaDon(savedHoaDon);
                    // Support idChiTietSanPham, idBienTheSanPham, and idBienThe (from banHangMain)
                    Integer idChiTietSanPhamValue = chiTietRequest.getIdChiTietSanPham();
                    if (idChiTietSanPhamValue == null) {
                        idChiTietSanPhamValue = chiTietRequest.getIdBienTheSanPham();
                    }
                    if (idChiTietSanPhamValue == null) {
                        idChiTietSanPhamValue = chiTietRequest.getIdBienThe();
                    }
                    if (idChiTietSanPhamValue == null) {
                        continue;
                    }
                    final Integer idChiTietSanPham = idChiTietSanPhamValue;
                    ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(idChiTietSanPham)
                            .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết sản phẩm với id: " + idChiTietSanPham,
                            "404"));
                    hoaDonChiTiet.setIdChiTietSanPham(chiTietSanPham);
                    hoaDonChiTiet.setSoLuong(chiTietRequest.getSoLuong() != null ? chiTietRequest.getSoLuong() : 1);
                    hoaDonChiTiet
                            .setGiaBan(chiTietRequest.getGiaBan() != null ? chiTietRequest.getGiaBan() : BigDecimal.ZERO);
                    // Calculate thanhTien if not provided
                    if (chiTietRequest.getThanhTien() != null) {
                        hoaDonChiTiet.setThanhTien(chiTietRequest.getThanhTien());
                    } else {
                        BigDecimal thanhTien = hoaDonChiTiet.getGiaBan()
                                .multiply(BigDecimal.valueOf(hoaDonChiTiet.getSoLuong()));
                        hoaDonChiTiet.setThanhTien(thanhTien);
                    }
                    hoaDonChiTiet.setTrangThai(chiTietRequest.getTrangThai() != null ? chiTietRequest.getTrangThai() : true);
                    hoaDonChiTiet.setGhiChu(chiTietRequest.getGhiChu());
                    hoaDonChiTiet.setDeleted(false);
                    hoaDonChiTietRepository.save(hoaDonChiTiet);
                }
                // Update tongTien after creating all chi tiết
                savedHoaDon = hoaDonRepository.findById(savedHoaDon.getId()).orElseThrow();
                BigDecimal totalTien = savedHoaDon.getHoaDonChiTiets().stream()
                        .filter(ct -> ct.getDeleted() == null || !ct.getDeleted())
                        .map(ct -> ct.getThanhTien() != null ? ct.getThanhTien() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (savedHoaDon.getTongTien() == null || savedHoaDon.getTongTien().compareTo(BigDecimal.ZERO) == 0) {
                    savedHoaDon.setTongTien(totalTien);
                    if (savedHoaDon.getTongTienSauGiam() == null
                            || savedHoaDon.getTongTienSauGiam().compareTo(BigDecimal.ZERO) == 0) {
                        savedHoaDon.setTongTienSauGiam(totalTien);
                    }
                    savedHoaDon = hoaDonRepository.save(savedHoaDon);
                }
            } catch (Exception e) {
                // Ignore detail creation errors; order creation should not stop here
            }
        }
        // Generate invoice code using stored procedure
        String generatedMaHoaDon = null;
        try {
            generatedMaHoaDon = generateInvoiceCode(savedHoaDon.getId());
            // Refresh entity to get updated ma_hoa_don and load HoaDonChiTiets from
            // database
            savedHoaDon = hoaDonRepository.findById(savedHoaDon.getId()).orElseThrow();
            // Force load HoaDonChiTiets collection (LAZY loading)
            if (savedHoaDon.getHoaDonChiTiets() != null) {
                savedHoaDon.getHoaDonChiTiets().size(); // Trigger lazy loading
            }
        } catch (Exception e) {
            // Fallback: generate temporary code if stored procedure fails
            if (savedHoaDon.getMaHoaDon() == null || savedHoaDon.getMaHoaDon().trim().isEmpty()) {
                String tempCode = "HD" + String.format("%010d", savedHoaDon.getId());
                savedHoaDon.setMaHoaDon(tempCode);
                savedHoaDon = hoaDonRepository.save(savedHoaDon);
            }
        }
        // Ensure maHoaDon is set before creating response
        if (savedHoaDon.getMaHoaDon() == null || savedHoaDon.getMaHoaDon().trim().isEmpty()) {
            String fallbackCode = generatedMaHoaDon != null ? generatedMaHoaDon
                    : ("HD" + String.format("%010d", savedHoaDon.getId()));
            savedHoaDon.setMaHoaDon(fallbackCode);
            savedHoaDon = hoaDonRepository.save(savedHoaDon);
            // Force load HoaDonChiTiets after save
            if (savedHoaDon.getHoaDonChiTiets() != null) {
                savedHoaDon.getHoaDonChiTiets().size(); // Trigger lazy loading
            }
        }
        // Create TimelineDonHang entry for order tracking
        try {
            // Use system admin or first available staff member if no staff assigned
            NhanVien timelineNhanVien = savedHoaDon.getIdNhanVien();
            if (timelineNhanVien == null) {
                timelineNhanVien = nhanVienRepository.findAll().stream()
                        .filter(nv -> nv.getTrangThai() != null && nv.getTrangThai())
                        .findFirst()
                        .orElse(null);
            }
            if (timelineNhanVien != null) {
                TimelineDonHang timeline = new TimelineDonHang();
                timeline.setIdHoaDon(savedHoaDon);
                timeline.setIdNhanVien(timelineNhanVien);
                timeline.setTrangThaiCu(null);
                timeline.setTrangThaiMoi("Tạo đơn hàng");
                timeline.setHanhDong("Tạo mới");
                timeline.setMoTa("Đơn hàng được tạo từ hệ thống bán hàng online");
                timeline.setGhiChu("Khách hàng đặt hàng online");
                timeline.setThoiGian(java.time.Instant.now());
                timeline.setTrangThai(true);
                timeline.setDeleted(false);
                timelineDonHangRepository.save(timeline);
            }
        } catch (Exception e) {
            // Ignore timeline creation failures; order creation should still succeed
        }
        // Create HinhThucThanhToan if payment method is provided
        if (request.getIdPhuongThucThanhToan() != null) {
            try {
                PhuongThucThanhToan phuongThucThanhToan = phuongThucThanhToanRepository
                        .findById(request.getIdPhuongThucThanhToan())
                        .orElse(null);
                if (phuongThucThanhToan != null) {
                    HinhThucThanhToan hinhThucThanhToan = new HinhThucThanhToan();
                    hinhThucThanhToan.setIdHoaDon(savedHoaDon);
                    hinhThucThanhToan.setIdPhuongThucThanhToan(phuongThucThanhToan);
                    // Determine payment type based on method name or ID
                    // COD (usually ID 1) uses cash, VNPAY uses bank transfer
                    String tenPhuongThuc = phuongThucThanhToan.getTenPhuongThucThanhToan() != null
                            ? phuongThucThanhToan.getTenPhuongThucThanhToan().toLowerCase()
                            : "";
                    boolean isCOD = request.getIdPhuongThucThanhToan() == 1
                            || tenPhuongThuc.contains("cod")
                            || tenPhuongThuc.contains("tiền mặt")
                            || tenPhuongThuc.contains("cash");
                    BigDecimal totalAmount = savedHoaDon.getTongTienSauGiam() != null
                            ? savedHoaDon.getTongTienSauGiam()
                            : (savedHoaDon.getTongTien() != null ? savedHoaDon.getTongTien() : BigDecimal.ZERO);
                    if (isCOD) {
                        hinhThucThanhToan.setTienMat(totalAmount);
                        hinhThucThanhToan.setTienChuyenKhoan(BigDecimal.ZERO);
                    } else {
                        hinhThucThanhToan.setTienChuyenKhoan(totalAmount);
                        hinhThucThanhToan.setTienMat(BigDecimal.ZERO);
                    }
                    hinhThucThanhToan.setTrangThai(true);
                    hinhThucThanhToan.setDeleted(false);
                    hinhThucThanhToanRepository.save(hinhThucThanhToan);
                }
            } catch (Exception e) {
                // Ignore payment method persistence errors to keep order flow intact
            }
        }
        // Final refresh to ensure all data is loaded before creating response
        savedHoaDon = hoaDonRepository.findById(savedHoaDon.getId()).orElseThrow();
        // Force load all lazy collections after creating all related entities
        try {
            if (savedHoaDon.getHoaDonChiTiets() != null) {
                savedHoaDon.getHoaDonChiTiets().size(); // Trigger lazy loading
            }
            if (savedHoaDon.getHinhThucThanhToans() != null) {
                savedHoaDon.getHinhThucThanhToans().size(); // Trigger lazy loading
            }
            if (savedHoaDon.getThongTinDonHangs() != null) {
                savedHoaDon.getThongTinDonHangs().size(); // Trigger lazy loading
            }
            if (savedHoaDon.getTimelineDonHangs() != null) {
                savedHoaDon.getTimelineDonHangs().size(); // Trigger lazy loading
            }
        } catch (Exception e) {
            // Ignore lazy loading issues; response is still generated
        }
        // Send order confirmation email once invoice is persisted
        sendOrderConfirmationEmail(savedHoaDon);
        // 🔔 NOTIFICATION: New order created
        try {
            // Notify staff member assigned to order
            if (savedHoaDon.getIdNhanVien() != null) {
                notificationService.createNotification(
                        savedHoaDon.getIdNhanVien().getId(),
                        "todo",
                        "Đơn hàng mới #" + savedHoaDon.getMaHoaDon(),
                        "Chờ xử lý",
                        "Đơn hàng mới từ "
                        + (savedHoaDon.getTenNguoiNhan() != null ? savedHoaDon.getTenNguoiNhan() : "khách hàng"),
                        2 // in progress
                );
            }
        } catch (Exception e) {
            // Ignore notification errors; order persists regardless
        }
        ThongTinDonHang thongTinDonHang = new ThongTinDonHang();
        thongTinDonHang.setIdHoaDon(savedHoaDon);
        thongTinDonHang.setIdTrangThaiDonHang(trangThaiDonHangRepository.findById(1).orElse(null));
        thongTinDonHang.setThoiGian(LocalDateTime.now());
        thongTinDonHang.setTrangThai(true);
        thongTinDonHang.setDeleted(false);
        thongTinDonHangRepository.save(thongTinDonHang);
        return new HoaDonResponse(savedHoaDon);
    }

    public HoaDonResponse update(Integer id, BanHangTaiQuayRequest request) {
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn", "404"));

        // Cập nhật thủ công từng field
        if (request.getTenNguoiNhan() != null) {
            hd.setTenNguoiNhan(request.getTenNguoiNhan());
        }
        if (request.getDiaChiNhanHang() != null) {
            hd.setDiaChiNguoiNhan(request.getDiaChiNhanHang());
        }
        if (request.getSoDienThoaiNguoiNhan() != null) {
            hd.setSoDienThoaiNguoiNhan(request.getSoDienThoaiNguoiNhan());
        }
        if (request.getEmailNguoiNhan() != null) {
            hd.setEmailNguoiNhan(request.getEmailNguoiNhan());
        }
        if (request.getTongTien() != null) {
            hd.setTongTien(request.getTongTien());
        }
        if (request.getTongTienSauGiam() != null) {
            hd.setTongTienSauGiam(request.getTongTienSauGiam());
        }
        if (request.getPhiVanChuyen() != null) {
            hd.setPhiVanChuyen(request.getPhiVanChuyen());
        }
        if (request.getLoaiDon() != null) {
            hd.setGiaoHang(request.getLoaiDon()); // ✅ update loại đơn
        }
        if (request.getTrangThai() != null) {
            hd.setTrangThai(request.getTrangThai());
        }
        if (request.getNgayTao() != null) {
            hd.setNgayTao(request.getNgayTao());
        }
        if (request.getNgayThanhToan() != null) {
            hd.setNgayThanhToan(request.getNgayThanhToan());
        }
        if (request.getGhiChu() != null) {
            hd.setGhiChu(request.getGhiChu());
        }
        if (request.getTenNhanVien() != null) {
            hd.setTenNhanVien(request.getTenNhanVien());
        }
        if (request.getMaNhanVien() != null) {
            hd.setMaNhanVien(request.getMaNhanVien());
        }
        if (request.getTenPhieuGiamGia() != null) {
            hd.setTenPhieuGiamGia(request.getTenPhieuGiamGia());
        }
        if (request.getMaPhieuGiamGia() != null) {
            hd.setMaPhieuGiamGia(request.getMaPhieuGiamGia());
        }

        // Gán lại các quan hệ
        if (request.getIdKhachHang() != null) {
            hd.setIdKhachHang(khachHangRepository.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new ApiException("Khách hàng không tồn tại", "404")));
        }
        if (request.getIdPhieuGiamGia() != null) {
            hd.setIdPhieuGiamGia(phieuGiamGiaService.getById(request.getIdPhieuGiamGia()));

            // Tự động điền tên phiếu giảm giá nếu chưa có (nhưng KHÔNG điền mã)
            if (hd.getIdPhieuGiamGia() != null) {
                if (hd.getTenPhieuGiamGia() == null || hd.getTenPhieuGiamGia().trim().isEmpty()) {
                    hd.setTenPhieuGiamGia(hd.getIdPhieuGiamGia().getTenPhieuGiamGia());
                }
            }
        }
        if (request.getIdNhanVien() != null) {
            hd.setIdNhanVien(nhanVienRepository.getById(request.getIdNhanVien()));

            // Tự động điền tên và mã nhân viên nếu chưa có
            if (hd.getIdNhanVien() != null) {
                if (hd.getTenNhanVien() == null || hd.getTenNhanVien().trim().isEmpty()) {
                    hd.setTenNhanVien(hd.getIdNhanVien().getTenNhanVien());
                }
                if (hd.getMaNhanVien() == null || hd.getMaNhanVien().trim().isEmpty()) {
                    hd.setMaNhanVien(hd.getIdNhanVien().getMaNhanVien());
                }
            }
        }
        hd.setUpdateAt(LocalDateTime.now());

        // Track original status and loaiDon before update
        Boolean originalStatus = hd.getTrangThai();
        Boolean originalLoaiDon = hd.getGiaoHang();

        HoaDon saved = hoaDonRepository.save(hd);

        // Get original idTrangThaiDonHang from latest ThongTinDonHang
        Integer originalIdTrangThaiDonHang = null;
        try {
            List<ThongTinDonHang> thongTinDonHangs = thongTinDonHangRepository
                    .findByHoaDonIdOrderByThoiGianDesc(saved.getId());
            if (thongTinDonHangs != null && !thongTinDonHangs.isEmpty()) {
                ThongTinDonHang latestThongTin = thongTinDonHangs.get(0);
                if (latestThongTin.getIdTrangThaiDonHang() != null) {
                    originalIdTrangThaiDonHang = latestThongTin.getIdTrangThaiDonHang().getId();
                }
            }
        } catch (Exception e) {
        }

        // ✅ VALIDATE & DEDUCT INVENTORY when status changes to "Đã xác nhận" (idTrangThaiDonHang = 2)
        if (request.getIdTrangThaiDonHang() != null && request.getIdTrangThaiDonHang() == 2) {
            // Status is changing to "Đã xác nhận" - validate and deduct inventory
            validateAndDeductInventory(saved);
        }

        // ✅ RESTORE INVENTORY when status changes to "Đã hủy" (idTrangThaiDonHang = 6)
        if (request.getIdTrangThaiDonHang() != null && request.getIdTrangThaiDonHang() == 6) {
            // Status is changing to "Đã hủy" - restore inventory and handle cancellation
            handleOrderCancellation(saved, originalIdTrangThaiDonHang);
        }

        // Get nhân viên for timeline (from request or from saved invoice)
        NhanVien timelineNhanVien = saved.getIdNhanVien();
        if (request.getIdNhanVien() != null) {
            timelineNhanVien = nhanVienRepository.findById(request.getIdNhanVien()).orElse(saved.getIdNhanVien());
        }
        if (timelineNhanVien == null) {
            // Fallback to first available staff
            timelineNhanVien = nhanVienRepository.findAll().stream()
                    .filter(nv -> nv.getTrangThai() != null && nv.getTrangThai())
                    .findFirst()
                    .orElse(null);
        }

        // Check if idTrangThaiDonHang changed
        boolean idTrangThaiDonHangChanged = request.getIdTrangThaiDonHang() != null
                && !request.getIdTrangThaiDonHang().equals(originalIdTrangThaiDonHang);

        // Create timeline entry when status changes (either boolean or
        // idTrangThaiDonHang)
        if (timelineNhanVien != null
                && ((request.getTrangThai() != null && !request.getTrangThai().equals(originalStatus))
                || idTrangThaiDonHangChanged)) {
            try {
                // Get original status text from latest ThongTinDonHang if available
                String trangThaiCu = "Chờ xác nhận"; // Default
                try {
                    List<ThongTinDonHang> thongTinDonHangs = thongTinDonHangRepository
                            .findByHoaDonIdOrderByThoiGianDesc(saved.getId());
                    if (thongTinDonHangs != null && !thongTinDonHangs.isEmpty()) {
                        ThongTinDonHang latestThongTin = thongTinDonHangs.get(0);
                        if (latestThongTin.getIdTrangThaiDonHang() != null) {
                            // Map idTrangThaiDonHang to status text
                            switch (latestThongTin.getIdTrangThaiDonHang().getId()) {
                                case 1:
                                    trangThaiCu = "Chờ xác nhận";
                                    break;
                                case 2:
                                    trangThaiCu = "Đã xác nhận";
                                    break;
                                case 3:
                                    trangThaiCu = "Đang xử lý";
                                    break;
                                case 4:
                                    trangThaiCu = "Đang giao hàng";
                                    break;
                                case 5:
                                    trangThaiCu = "Đã giao hàng";
                                    break;
                                case 6:
                                    trangThaiCu = "Đã hủy";
                                    break;
                                case 7:
                                    trangThaiCu = "Hoàn thành";
                                    break;
                                default:
                                    trangThaiCu = originalStatus != null && originalStatus ? "Hoàn thành" : "Chờ xác nhận";
                                    break;
                            }
                        } else {
                            trangThaiCu = originalStatus != null && originalStatus ? "Hoàn thành" : "Chờ xác nhận";
                        }
                    } else {
                        trangThaiCu = originalStatus != null && originalStatus ? "Hoàn thành" : "Chờ xác nhận";
                    }
                } catch (Exception e) {
                    trangThaiCu = originalStatus != null && originalStatus ? "Hoàn thành" : "Chờ xác nhận";
                }

                // Get new status text from idTrangThaiDonHang if provided, otherwise map from
                // boolean
                String trangThaiMoi;

                if (request.getIdTrangThaiDonHang() != null) {
                    // Map idTrangThaiDonHang to status text
                    switch (request.getIdTrangThaiDonHang()) {
                        case 1:
                            trangThaiMoi = "Chờ xác nhận";
                            break;
                        case 2:
                            trangThaiMoi = "Đã xác nhận";
                            break;
                        case 3:
                            trangThaiMoi = "Đang xử lý";
                            break;
                        case 4:
                            trangThaiMoi = "Đang giao hàng";
                            break;
                        case 5:
                            trangThaiMoi = "Đã giao hàng";
                            break;
                        case 6:
                            trangThaiMoi = "Đã hủy";
                            break;
                        case 7:
                            trangThaiMoi = "Hoàn thành";
                            break;
                        default:
                            trangThaiMoi = saved.getTrangThai() ? "Hoàn thành" : "Chờ xác nhận";
                            break;
                    }
                } else {
                    // Fallback: map boolean to status text
                    trangThaiMoi = saved.getTrangThai() ? "Hoàn thành" : "Chờ xác nhận";
                }
                // Update ThongTinDonHang with corresponding idTrangThaiDonHang
                // idTrangThaiDonHang: 1 = Chờ xác nhận, 2 = Đã xác nhận, 3 = Đang xử lý, 4 =
                // Đang giao hàng, 5 = Đã giao hàng, 6 = Đã hủy, 7 = Hoàn thành
                try {
                    // Use idTrangThaiDonHang from request if provided, otherwise map from boolean
                    Integer requestedIdTrangThaiDonHang = request.getIdTrangThaiDonHang();
                    final Integer idTrangThaiDonHang;
                    if (requestedIdTrangThaiDonHang != null) {
                        idTrangThaiDonHang = requestedIdTrangThaiDonHang;
                    } else {
                        // Fallback: map boolean to: false = 1 (Chờ xác nhận), true = 7 (Hoàn thành)
                        idTrangThaiDonHang = saved.getTrangThai() ? 7 : 1;
                    }

                    // Validate idTrangThaiDonHang exists
                    if (!trangThaiDonHangRepository.existsById(idTrangThaiDonHang)) {
                    } else {
                        // Create new ThongTinDonHang entry for status change
                        if (hd.getGiaoHang() && !hd.getGhiChu().contains("Bán hàng tại quầy")
                                && trangThaiMoi.equals("Hoàn thành")) {
                            hd.setSoTienDaThanhToan(hd.getTongTienSauGiam());
                            saved = hoaDonRepository.save(hd);
                        }
                        ThongTinDonHang newThongTin = new ThongTinDonHang();
                        newThongTin.setIdHoaDon(saved);
                        newThongTin.setIdTrangThaiDonHang(trangThaiDonHangRepository.findById(idTrangThaiDonHang)
                                .orElseThrow(() -> new ApiException(
                                "Không tìm thấy trạng thái đơn hàng với id: " + idTrangThaiDonHang, "404")));
                        newThongTin.setThoiGian(LocalDateTime.now());
                        newThongTin.setTrangThai(true);
                        newThongTin.setDeleted(false);
                        thongTinDonHangRepository.save(newThongTin);
                    }
                } catch (Exception e) {
                    // Don't throw - timeline is already created, main update should succeed
                }
            } catch (Exception e) {
            }
        }

        // Create timeline entry when loaiDon (giaoHang) changes
        if (request.getLoaiDon() != null && !request.getLoaiDon().equals(originalLoaiDon) && timelineNhanVien != null) {
            try {
                String loaiDonCu = originalLoaiDon != null && originalLoaiDon ? "online" : "tại quầy";
                String loaiDonMoi = saved.getGiaoHang() ? "online" : "tại quầy";
                String hanhDong = "Cập nhật";
                String moTa = "Cập nhật loại đơn từ \"" + loaiDonCu + "\" sang \"" + loaiDonMoi + "\"";

                TimelineDonHang timeline = new TimelineDonHang();
                timeline.setIdHoaDon(saved);
                timeline.setIdNhanVien(timelineNhanVien);
                timeline
                        .setTrangThaiCu(saved.getTrangThai() != null && saved.getTrangThai() ? "Hoàn thành" : "Chờ xác nhận");
                timeline.setTrangThaiMoi(
                        saved.getTrangThai() != null && saved.getTrangThai() ? "Hoàn thành" : "Chờ xác nhận");
                timeline.setHanhDong(hanhDong);
                timeline.setMoTa(moTa);
                timeline.setGhiChu("Loại đơn: " + loaiDonMoi);
                timeline.setThoiGian(java.time.Instant.now());
                timeline.setTrangThai(true);
                timeline.setDeleted(false);
                timelineDonHangRepository.save(timeline);
            } catch (Exception e) {
            }
        }

        // NOTIFICATION: Order status updated
        if (request.getTrangThai() != null && !request.getTrangThai().equals(originalStatus)
                && saved.getIdKhachHang() != null) {
            try {
                String statusText = saved.getTrangThai() ? "Đã hoàn thành" : "Đang xử lý";
                notificationService.createNotification(
                        saved.getIdKhachHang().getId(),
                        "notice",
                        "Cập nhật đơn hàng #" + saved.getMaHoaDon(),
                        statusText,
                        "Trạng thái đơn hàng của bạn: " + statusText,
                        saved.getTrangThai() ? 1 : 2 // 1 = completed, 2 = in progress
                );
            } catch (Exception e) {

            }
        }

        return new HoaDonResponse(saved);
    }

    public void delete(Integer id) {
        HoaDon hd = hoaDonRepository.findById(id).orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn", "404"));
        hd.setDeleted(true);
        hoaDonRepository.save(hd);
    }

    /**
     * ✅ Validate and deduct product inventory when order is confirmed Throws
     * ApiException if insufficient inventory
     *
     * @param hoaDon - The invoice/order to process
     */
    private void validateAndDeductInventory(HoaDon hoaDon) {
        try {
            // Get order items (using query to avoid lazy loading issues)
            List<HoaDonChiTiet> orderItems = hoaDonChiTietRepository.findAllByIdHoaDonAndTrangThai(hoaDon, true);

            if (orderItems == null || orderItems.isEmpty()) {
                return;
            }

            // First pass: VALIDATE that we have enough inventory for all items
            StringBuilder insufficientItems = new StringBuilder();
            for (HoaDonChiTiet orderItem : orderItems) {
                if (orderItem.getDeleted() != null && orderItem.getDeleted()) {
                    continue; // Skip deleted items
                }

                ChiTietSanPham product = orderItem.getIdChiTietSanPham();
                if (product == null) {
                    continue;
                }

                Integer requiredQty = orderItem.getSoLuong();
                Integer availableQty = product.getSoLuong();

                if (availableQty == null) {
                    availableQty = 0;
                }
                if (requiredQty == null) {
                    requiredQty = 0;
                }

                if (availableQty < requiredQty) {
                    if (insufficientItems.length() > 0) {
                        insufficientItems.append("; ");
                    }
                    insufficientItems.append(String.format(
                            "%s: yêu cầu %d, có %d",
                            product.getTenChiTietSanPham() != null ? product.getTenChiTietSanPham() : "Sản phẩm",
                            requiredQty,
                            availableQty
                    ));
                }
            }

            // If any items have insufficient inventory, send notification email and throw error
            if (insufficientItems.length() > 0) {
                String errorMessage = "Số lượng sản phẩm yêu cầu không đủ: " + insufficientItems.toString();
                // Send email notification to customer about inventory shortage
                sendInventoryShortageNotificationEmail(hoaDon, insufficientItems.toString());

                throw new ApiException(errorMessage, "INSUFFICIENT_INVENTORY");
            }

            // Second pass: DEDUCT inventory for all items
            for (HoaDonChiTiet orderItem : orderItems) {
                if (orderItem.getDeleted() != null && orderItem.getDeleted()) {
                    continue; // Skip deleted items
                }

                ChiTietSanPham product = orderItem.getIdChiTietSanPham();
                if (product == null) {
                    continue;
                }

                Integer beforeQty = product.getSoLuong();
                Integer deductQty = orderItem.getSoLuong();

                if (deductQty == null || deductQty <= 0) {
                    continue;
                }

                // Deduct quantity
                Integer afterQty = beforeQty - deductQty;
                product.setSoLuong(afterQty);
                chiTietSanPhamRepository.save(product);
            }
        } catch (ApiException ae) {
            // Re-throw API exceptions as-is
            throw ae;
        } catch (Exception e) {
            throw new ApiException("Lỗi khi kiểm tra tồn kho: " + e.getMessage(), "INVENTORY_ERROR");
        }
    }

    /**
     * ✅ Handle order cancellation - restore inventory and remove revenue
     *
     * @param hoaDon - The invoice/order to process
     * @param originalIdTrangThaiDonHang - The original status ID before
     * cancellation
     */
    private void handleOrderCancellation(HoaDon hoaDon, Integer originalIdTrangThaiDonHang) {
        try {
            // Get order items (using query to avoid lazy loading issues)
            List<HoaDonChiTiet> orderItems = hoaDonChiTietRepository.findAllByIdHoaDonAndTrangThai(hoaDon, true);

            if (orderItems == null || orderItems.isEmpty()) {
                return;
            }

            // First pass: RESTORE inventory for all items
            for (HoaDonChiTiet orderItem : orderItems) {
                if (orderItem.getDeleted() != null && orderItem.getDeleted()) {
                    continue; // Skip deleted items
                }

                ChiTietSanPham product = orderItem.getIdChiTietSanPham();
                if (product == null) {
                    continue;
                }

                Integer beforeQty = product.getSoLuong();
                Integer restoreQty = orderItem.getSoLuong();

                if (restoreQty == null || restoreQty <= 0) {
                    continue;
                }

                // Restore quantity
                Integer afterQty = beforeQty + restoreQty;
                product.setSoLuong(afterQty);
                chiTietSanPhamRepository.save(product);
            }

            // Second pass: REMOVE revenue if original status was "Hoàn thành" (idTrangThaiDonHang = 7)
            if (originalIdTrangThaiDonHang != null && originalIdTrangThaiDonHang == 7) {
                BigDecimal totalRevenue = hoaDon.getTongTienSauGiam() != null
                        ? hoaDon.getTongTienSauGiam()
                        : (hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO);

                // Update total revenue in the system (e.g., subtract from total revenue)
                // This is a placeholder - implement your own logic to update total revenue
                // Example: totalRevenueService.subtractRevenue(totalRevenue);
            }

            String customerEmail = resolveCustomerEmail(hoaDon);
            String customerName = resolveCustomerName(hoaDon);

            if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                OrderEmailData cancellationEmail = buildOrderStatusEmailData(hoaDon, orderItems, "Đã hủy",
                        customerName, customerEmail);
                emailService.sendOrderStatusUpdateEmail(cancellationEmail);
            }

            BigDecimal voucherValue = hoaDon.getTongTienSauGiam() != null
                    ? hoaDon.getTongTienSauGiam()
                    : (hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO);

            if (isOrderPaid(hoaDon) && voucherValue.compareTo(BigDecimal.ZERO) > 0) {
                VoucherInfo voucherInfo = createCancellationVoucher(hoaDon, voucherValue, customerName);

                if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                    VoucherEmailData voucherEmail = VoucherEmailData.builder()
                            .customerName(customerName)
                            .customerEmail(customerEmail)
                            .voucherCode(voucherInfo.code())
                            .voucherName("Voucher Bồi Hoàn Hủy Đơn")
                            .voucherType("FIXED_AMOUNT")
                            .discountValue(voucherValue)
                            .maxDiscount(voucherValue)
                            .minOrderValue(BigDecimal.ZERO)
                            .validFrom(voucherInfo.issuedAt())
                            .validUntil(voucherInfo.expiry())
                            .usageLimit(1)
                            .description("Voucher bồi hoàn cho đơn hàng đã thanh toán bị hủy")
                            .build();
                    emailService.sendVoucherAssignmentEmail(voucherEmail);
                }
            }
        } catch (Exception e) {
            throw new ApiException("Lỗi khi xử lý hủy đơn hàng: " + e.getMessage(), "CANCELLATION_ERROR");
        }
    }

    private String resolveCustomerEmail(HoaDon hoaDon) {
        if (hoaDon.getEmailNguoiNhan() != null && !hoaDon.getEmailNguoiNhan().trim().isEmpty()) {
            return hoaDon.getEmailNguoiNhan().trim();
        }

        if (hoaDon.getIdKhachHang() != null && hoaDon.getIdKhachHang().getEmail() != null
                && !hoaDon.getIdKhachHang().getEmail().trim().isEmpty()) {
            return hoaDon.getIdKhachHang().getEmail().trim();
        }

        return null;
    }

    private String resolveCustomerName(HoaDon hoaDon) {
        if (hoaDon.getTenNguoiNhan() != null && !hoaDon.getTenNguoiNhan().trim().isEmpty()) {
            return hoaDon.getTenNguoiNhan().trim();
        }

        if (hoaDon.getIdKhachHang() != null && hoaDon.getIdKhachHang().getTenKhachHang() != null
                && !hoaDon.getIdKhachHang().getTenKhachHang().trim().isEmpty()) {
            return hoaDon.getIdKhachHang().getTenKhachHang().trim();
        }

        return "Khách hàng";
    }

    private boolean isOrderPaid(HoaDon hoaDon) {
        if (hoaDon == null) {
            return false;
        }

        if (Boolean.TRUE.equals(hoaDon.getTrangThaiThanhToan())) {
            return true;
        }

        BigDecimal soTienDaThanhToan = hoaDon.getSoTienDaThanhToan();
        BigDecimal soTienConLai = hoaDon.getSoTienConLai();

        if (soTienDaThanhToan != null && soTienDaThanhToan.compareTo(BigDecimal.ZERO) > 0) {
            return soTienConLai == null || soTienConLai.compareTo(BigDecimal.ZERO) <= 0;
        }

        return false;
    }

    private OrderEmailData buildOrderStatusEmailData(HoaDon hoaDon, List<HoaDonChiTiet> orderItems, String statusName,
            String customerName, String customerEmail) {

        String resolvedStatus = (statusName != null && !statusName.isBlank()) ? statusName : "Cập nhật đơn hàng";

        List<OrderEmailData.OrderItemData> items = new ArrayList<>();
        if (orderItems != null && !orderItems.isEmpty()) {
            for (HoaDonChiTiet item : orderItems) {
                if (item == null) {
                    continue;
                }

                String productName = "Sản phẩm";
                BigDecimal price = item.getGiaBan() != null ? item.getGiaBan() : BigDecimal.ZERO;
                Integer quantity = item.getSoLuong() != null ? item.getSoLuong() : 0;

                if (item.getIdChiTietSanPham() != null && item.getIdChiTietSanPham().getIdSanPham() != null
                        && item.getIdChiTietSanPham().getIdSanPham().getTenSanPham() != null) {
                    productName = item.getIdChiTietSanPham().getIdSanPham().getTenSanPham();
                }

                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity.longValue()));

                items.add(OrderEmailData.OrderItemData.builder()
                        .productName(productName)
                        .quantity(quantity)
                        .price(price)
                        .subtotal(subtotal)
                        .build());
            }
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (hoaDon.getTongTien() != null && hoaDon.getTongTienSauGiam() != null) {
            discountAmount = hoaDon.getTongTien().subtract(hoaDon.getTongTienSauGiam());
            if (hoaDon.getPhiVanChuyen() != null) {
                discountAmount = discountAmount.subtract(hoaDon.getPhiVanChuyen());
            }
        }

        BigDecimal shippingFee = hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO;
        BigDecimal totalAmount = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
        BigDecimal finalAmount = hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : totalAmount;

        return OrderEmailData.builder()
                .orderCode(hoaDon.getMaHoaDon())
                .customerName(customerName != null ? customerName : "Khách hàng")
                .customerEmail(customerEmail)
                .orderDate(hoaDon.getNgayTao() != null ? hoaDon.getNgayTao() : LocalDateTime.now())
                .orderStatus(resolvedStatus)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .shippingFee(shippingFee)
                .finalAmount(finalAmount)
                .deliveryAddress(hoaDon.getDiaChiNguoiNhan() != null ? hoaDon.getDiaChiNguoiNhan() : "")
                .phoneNumber(hoaDon.getSoDienThoaiNguoiNhan() != null ? hoaDon.getSoDienThoaiNguoiNhan() : "")
                .items(items)
                .build();
    }

    private VoucherInfo createCancellationVoucher(HoaDon hoaDon, BigDecimal voucherValue, String customerName) {
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiry = issuedAt.plusMonths(3);
        String voucherCode = generateVoucherCode(hoaDon.getId(), "CAN");
        String resolvedName = customerName != null ? customerName : "Khách hàng";

        return persistVoucherForOrder(
                hoaDon,
                voucherValue,
                voucherCode,
                "Voucher Hoàn Tiền Hủy Đơn - " + resolvedName,
                "Voucher bồi hoàn cho đơn hàng đã thanh toán bị hủy.",
                issuedAt,
                expiry);
    }

    /**
     * Helper method to send order confirmation email
     */
    private String generateInvoiceCode(Integer idHoaDon) {
        try {
            String sql = "DECLARE @maHoaDon NVARCHAR(12); EXEC sp_GenerateMaHoaDon @idHoaDon = ?, @maMoiGenerated = @maHoaDon OUTPUT; SELECT @maHoaDon as ma_hoa_don";
            return jdbcTemplate.queryForObject(sql, String.class, idHoaDon);
        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice code: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to send order confirmation email
     */
    private void sendOrderConfirmationEmail(HoaDon hoaDon) {
        try {
            // Get customer email - prefer order email, fallback to customer email
            String customerEmail = hoaDon.getEmailNguoiNhan();
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                if (hoaDon.getIdKhachHang() != null && hoaDon.getIdKhachHang().getEmail() != null) {
                    customerEmail = hoaDon.getIdKhachHang().getEmail();
                }
            }

            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                return;
            }

            // Build order items list
            List<OrderEmailData.OrderItemData> items = new ArrayList<>();
            if (hoaDon.getHoaDonChiTiets() != null && !hoaDon.getHoaDonChiTiets().isEmpty()) {
                items = hoaDon.getHoaDonChiTiets().stream()
                        .map(item -> {
                            String productName = "Sản phẩm";
                            BigDecimal price = BigDecimal.ZERO;
                            if (item.getIdChiTietSanPham() != null) {
                                if (item.getIdChiTietSanPham().getIdSanPham() != null) {
                                    productName = item.getIdChiTietSanPham().getIdSanPham().getTenSanPham();
                                }
                                price = item.getGiaBan() != null ? item.getGiaBan() : BigDecimal.ZERO;
                            }
                            Integer quantity = item.getSoLuong() != null ? item.getSoLuong() : 0;
                            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));

                            return OrderEmailData.OrderItemData.builder()
                                    .productName(productName)
                                    .quantity(quantity)
                                    .price(price)
                                    .subtotal(subtotal)
                                    .build();
                        })
                        .collect(Collectors.toList());
            }

            // Calculate discount amount
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (hoaDon.getTongTien() != null && hoaDon.getTongTienSauGiam() != null) {
                discountAmount = hoaDon.getTongTien().subtract(hoaDon.getTongTienSauGiam());
                if (hoaDon.getPhiVanChuyen() != null) {
                    discountAmount = discountAmount.subtract(hoaDon.getPhiVanChuyen());
                }
            }

            OrderEmailData emailData = OrderEmailData.builder()
                    .orderCode(hoaDon.getMaHoaDon())
                    .customerName(hoaDon.getTenNguoiNhan() != null ? hoaDon.getTenNguoiNhan() : "Khách hàng")
                    .customerEmail(customerEmail)
                    .orderDate(hoaDon.getNgayTao() != null ? hoaDon.getNgayTao() : LocalDateTime.now())
                    .totalAmount(hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO)
                    .discountAmount(discountAmount)
                    .shippingFee(hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO)
                    .finalAmount(hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : BigDecimal.ZERO)
                    .deliveryAddress(hoaDon.getDiaChiNguoiNhan() != null ? hoaDon.getDiaChiNguoiNhan() : "")
                    .phoneNumber(hoaDon.getSoDienThoaiNguoiNhan() != null ? hoaDon.getSoDienThoaiNguoiNhan() : "")
                    .items(items)
                    .build();

            emailService.sendOrderConfirmationEmail(emailData);
        } catch (Exception e) {
            // Don't throw exception - we don't want to rollback the order creation
        }
    }

    /**
     * Send inventory shortage notification email to customer Informs customer
     * that store is trying to fulfill the order but some products are out of
     * stock
     *
     * @param hoaDon The order with insufficient inventory
     * @param insufficientProductDetails Details of products with insufficient
     * stock
     */
    private void sendInventoryShortageNotificationEmail(HoaDon hoaDon, String insufficientProductDetails) {
        try {
            // Get customer email
            String customerEmail = hoaDon.getEmailNguoiNhan();
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                if (hoaDon.getIdKhachHang() != null && hoaDon.getIdKhachHang().getEmail() != null) {
                    customerEmail = hoaDon.getIdKhachHang().getEmail();
                }
            }

            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                return;
            }

            // Build customer name
            String customerName = hoaDon.getTenNguoiNhan() != null ? hoaDon.getTenNguoiNhan() : "Khách hàng";

            // Create email data object
            OrderEmailData emailData = OrderEmailData.builder()
                    .orderCode(hoaDon.getMaHoaDon())
                    .customerName(customerName)
                    .customerEmail(customerEmail)
                    .orderDate(hoaDon.getNgayTao() != null ? hoaDon.getNgayTao() : LocalDateTime.now())
                    .orderStatus("Sự cố về số lượng - " + insufficientProductDetails)
                    .totalAmount(hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO)
                    .finalAmount(hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : BigDecimal.ZERO)
                    .deliveryAddress(hoaDon.getDiaChiNguoiNhan() != null ? hoaDon.getDiaChiNguoiNhan() : "")
                    .phoneNumber(hoaDon.getSoDienThoaiNguoiNhan() != null ? hoaDon.getSoDienThoaiNguoiNhan() : "")
                    .build();
            // Send email notification asynchronously
            emailService.sendInventoryShortageNotificationEmail(emailData);
        } catch (Exception e) {
            // Don't throw exception - we still want to reject the order even if email fails
        }
    }

    /**
     * Thêm dữ liệu mẫu cho 3 hóa đơn
     */
    public void addSampleData() {
        try {
            // Lấy nhân viên đầu tiên
            NhanVien nhanVien = nhanVienRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "404"));

            // Lấy khách hàng đầu tiên
            KhachHang khachHang = khachHangRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "404"));

            // Lấy chi tiết sản phẩm đầu tiên
            ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết sản phẩm", "404"));

            // Tạo 3 hóa đơn mẫu
            for (int i = 1; i <= 3; i++) {
                HoaDon hoaDon = new HoaDon();
                hoaDon.setIdKhachHang(khachHang);
                hoaDon.setIdNhanVien(nhanVien);
                hoaDon.setTenHoaDon("Hóa đơn mẫu " + i);
                hoaDon.setGiaoHang(false); // Tại quầy
                hoaDon.setPhiVanChuyen(BigDecimal.ZERO);
                hoaDon.setTongTien(BigDecimal.valueOf(1000000 * i));
                hoaDon.setTongTienSauGiam(BigDecimal.valueOf(1000000 * i));
                hoaDon.setTenNguoiNhan("Khách hàng " + i);
                hoaDon.setDiaChiNguoiNhan("Địa chỉ " + i + ", TP.HCM");
                hoaDon.setSoDienThoaiNguoiNhan("012345678" + i);
                hoaDon.setEmailNguoiNhan("khachhang" + i + "@email.com");
                hoaDon.setNgayTao(LocalDateTime.now());
                hoaDon.setTrangThai(true); // Hoàn thành
                hoaDon.setDeleted(false);

                HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

                // Tạo chi tiết hóa đơn
                HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
                hoaDonChiTiet.setIdHoaDon(savedHoaDon);
                hoaDonChiTiet.setIdChiTietSanPham(chiTietSanPham);
                hoaDonChiTiet.setSoLuong(i);
                hoaDonChiTiet.setGiaBan(BigDecimal.valueOf(1000000));
                hoaDonChiTiet.setThanhTien(BigDecimal.valueOf(1000000 * i));
                hoaDonChiTiet.setTrangThai(true);
                hoaDonChiTiet.setGhiChu("Sản phẩm mẫu " + i + " - Màu đen - Size 42");
                hoaDonChiTiet.setDeleted(false);
                hoaDonChiTietRepository.save(hoaDonChiTiet);
            }

        } catch (Exception e) {
            throw new ApiException("Lỗi khi thêm dữ liệu mẫu: " + e.getMessage(), "500");
        }
    }

    /**
     * Helper method to send order confirmation email
     */
    /**
     * Thống kê doanh thu chỉ từ các đơn hàng có trạng thái CUỐI CÙNG là hoàn
     * thành (idTrangThaiDonHang = 7)
     */
    public Map<String, Object> getCompletedOrderRevenue(String startDate, String endDate, String groupBy) {
        try {
            StringBuilder sql = new StringBuilder();

            if ("day".equals(groupBy)) {
                sql.append("SELECT ")
                        .append("CONVERT(DATE, hd.ngay_tao) as ngay, ")
                        .append("COUNT(*) as so_don_hang, ")
                        .append("SUM(hd.tong_tien_sau_giam) as doanh_thu ")
                        .append("FROM hoa_don hd ")
                        .append("WHERE hd.id IN ( ")
                        .append("    SELECT DISTINCT ttdh.id_hoa_don ")
                        .append("    FROM thong_tin_don_hang ttdh ")
                        .append("    INNER JOIN ( ")
                        .append("        SELECT id_hoa_don, MAX(thoi_gian) as max_thoi_gian ")
                        .append("        FROM thong_tin_don_hang ")
                        .append("        WHERE deleted = 0 ")
                        .append("        GROUP BY id_hoa_don ")
                        .append("    ) latest ON ttdh.id_hoa_don = latest.id_hoa_don AND ttdh.thoi_gian = latest.max_thoi_gian ")
                        .append("    WHERE ttdh.id_trang_thai_don_hang = 7 AND ttdh.deleted = 0 ")
                        .append(") ")
                        .append("AND hd.deleted = 0 ");
            } else if ("month".equals(groupBy)) {
                sql.append("SELECT ")
                        .append("YEAR(hd.ngay_tao) as nam, ")
                        .append("MONTH(hd.ngay_tao) as thang, ")
                        .append("COUNT(*) as so_don_hang, ")
                        .append("SUM(hd.tong_tien_sau_giam) as doanh_thu ")
                        .append("FROM hoa_don hd ")
                        .append("WHERE hd.id IN ( ")
                        .append("    SELECT DISTINCT ttdh.id_hoa_don ")
                        .append("    FROM thong_tin_don_hang ttdh ")
                        .append("    INNER JOIN ( ")
                        .append("        SELECT id_hoa_don, MAX(thoi_gian) as max_thoi_gian ")
                        .append("        FROM thong_tin_don_hang ")
                        .append("        WHERE deleted = 0 ")
                        .append("        GROUP BY id_hoa_don ")
                        .append("    ) latest ON ttdh.id_hoa_don = latest.id_hoa_don AND ttdh.thoi_gian = latest.max_thoi_gian ")
                        .append("    WHERE ttdh.id_trang_thai_don_hang = 7 AND ttdh.deleted = 0 ")
                        .append(") ")
                        .append("AND hd.deleted = 0 ");
            } else { // year
                sql.append("SELECT ")
                        .append("YEAR(hd.ngay_tao) as nam, ")
                        .append("COUNT(*) as so_don_hang, ")
                        .append("SUM(hd.tong_tien_sau_giam) as doanh_thu ")
                        .append("FROM hoa_don hd ")
                        .append("WHERE hd.id IN ( ")
                        .append("    SELECT DISTINCT ttdh.id_hoa_don ")
                        .append("    FROM thong_tin_don_hang ttdh ")
                        .append("    INNER JOIN ( ")
                        .append("        SELECT id_hoa_don, MAX(thoi_gian) as max_thoi_gian ")
                        .append("        FROM thong_tin_don_hang ")
                        .append("        WHERE deleted = 0 ")
                        .append("        GROUP BY id_hoa_don ")
                        .append("    ) latest ON ttdh.id_hoa_don = latest.id_hoa_don AND ttdh.thoi_gian = latest.max_thoi_gian ")
                        .append("    WHERE ttdh.id_trang_thai_don_hang = 7 AND ttdh.deleted = 0 ")
                        .append(") ")
                        .append("AND hd.deleted = 0 ");
            }

            // Thêm điều kiện thời gian nếu có
            if (startDate != null && !startDate.trim().isEmpty()) {
                sql.append("AND hd.ngay_tao >= ? ");
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                sql.append("AND hd.ngay_tao <= ? ");
            }

            if ("day".equals(groupBy)) {
                sql.append("GROUP BY CONVERT(DATE, hd.ngay_tao) ")
                        .append("ORDER BY CONVERT(DATE, hd.ngay_tao) DESC");
            } else if ("month".equals(groupBy)) {
                sql.append("GROUP BY YEAR(hd.ngay_tao), MONTH(hd.ngay_tao) ")
                        .append("ORDER BY YEAR(hd.ngay_tao) DESC, MONTH(hd.ngay_tao) DESC");
            } else {
                sql.append("GROUP BY YEAR(hd.ngay_tao) ")
                        .append("ORDER BY YEAR(hd.ngay_tao) DESC");
            }

            List<Map<String, Object>> results;

            // Thực hiện query với parameters
            if (startDate != null && !startDate.trim().isEmpty() && endDate != null && !endDate.trim().isEmpty()) {
                results = jdbcTemplate.queryForList(sql.toString(), startDate, endDate);
            } else if (startDate != null && !startDate.trim().isEmpty()) {
                results = jdbcTemplate.queryForList(sql.toString(), startDate);
            } else if (endDate != null && !endDate.trim().isEmpty()) {
                results = jdbcTemplate.queryForList(sql.toString(), endDate);
            } else {
                results = jdbcTemplate.queryForList(sql.toString());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", results);
            response.put("groupBy", groupBy);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("totalRecords", results.size());

            return response;

        } catch (Exception e) {
            throw new ApiException("Lỗi khi lấy thống kê doanh thu: " + e.getMessage(), "500");
        }
    }

    /**
     * Thống kê dashboard tổng quan chỉ tính đơn hàng có trạng thái CUỐI CÙNG là
     * hoàn thành
     */
    public Map<String, Object> getCompletedOrderDashboard() {
        try {
            Map<String, Object> dashboard = new HashMap<>();

            // Tổng doanh thu từ đơn hàng có trạng thái cuối cùng là hoàn thành
            String totalRevenueSQL = """
                    SELECT COALESCE(SUM(hd.tong_tien_sau_giam), 0) as total_revenue
                    FROM hoa_don hd 
                    WHERE hd.id IN (
                        SELECT DISTINCT ttdh.id_hoa_don
                        FROM thong_tin_don_hang ttdh
                        INNER JOIN (
                            SELECT id_hoa_don, MAX(thoi_gian) as max_thoi_gian
                            FROM thong_tin_don_hang
                            WHERE deleted = 0
                            GROUP BY id_hoa_don
                        ) latest ON ttdh.id_hoa_don = latest.id_hoa_don AND ttdh.thoi_gian = latest.max_thoi_gian
                        WHERE ttdh.id_trang_thai_don_hang = 7 AND ttdh.deleted = 0
                    )
                    AND hd.deleted = 0
                    """;

            BigDecimal totalRevenue = jdbcTemplate.queryForObject(totalRevenueSQL, BigDecimal.class);
            dashboard.put("totalRevenue", totalRevenue);

            // Số đơn hàng có trạng thái cuối cùng là hoàn thành
            String completedOrdersSQL = """
                    SELECT COUNT(*) as completed_orders
                    FROM hoa_don hd 
                    WHERE hd.id IN (
                        SELECT DISTINCT ttdh.id_hoa_don
                        FROM thong_tin_don_hang ttdh
                        INNER JOIN (
                            SELECT id_hoa_don, MAX(thoi_gian) as max_thoi_gian
                            FROM thong_tin_don_hang
                            WHERE deleted = 0
                            GROUP BY id_hoa_don
                        ) latest ON ttdh.id_hoa_don = latest.id_hoa_don AND ttdh.thoi_gian = latest.max_thoi_gian
                        WHERE ttdh.id_trang_thai_don_hang = 7 AND ttdh.deleted = 0
                    )
                    AND hd.deleted = 0
                    """;

            Integer completedOrders = jdbcTemplate.queryForObject(completedOrdersSQL, Integer.class);
            dashboard.put("completedOrders", completedOrders);

            // Doanh thu hôm nay (chỉ đơn có trạng thái cuối cùng là hoàn thành)
            String todayRevenueSQL = """
                    SELECT COALESCE(SUM(hd.tong_tien_sau_giam), 0) as today_revenue
                    FROM hoa_don hd 
                    WHERE hd.id IN (
                        SELECT DISTINCT ttdh.id_hoa_don
                        FROM thong_tin_don_hang ttdh
                        INNER JOIN (
                            SELECT id_hoa_don, MAX(thoi_gian) as max_thoi_gian
                            FROM thong_tin_don_hang
                            WHERE deleted = 0
                            GROUP BY id_hoa_don
                        ) latest ON ttdh.id_hoa_don = latest.id_hoa_don AND ttdh.thoi_gian = latest.max_thoi_gian
                        WHERE ttdh.id_trang_thai_don_hang = 7 AND ttdh.deleted = 0
                    )
                    AND CONVERT(DATE, hd.ngay_tao) = CONVERT(DATE, GETDATE())
                    AND hd.deleted = 0
                    """;

            BigDecimal todayRevenue = jdbcTemplate.queryForObject(todayRevenueSQL, BigDecimal.class);
            dashboard.put("todayRevenue", todayRevenue);

            // Doanh thu tháng này (chỉ đơn có trạng thái cuối cùng là hoàn thành)
            String monthRevenueSQL = """
                    SELECT COALESCE(SUM(hd.tong_tien_sau_giam), 0) as month_revenue
                    FROM hoa_don hd 
                    WHERE hd.id IN (
                        SELECT DISTINCT ttdh.id_hoa_don
                        FROM thong_tin_don_hang ttdh
                        INNER JOIN (
                            SELECT id_hoa_don, MAX(thoi_gian) as max_thoi_gian
                            FROM thong_tin_don_hang
                            WHERE deleted = 0
                            GROUP BY id_hoa_don
                        ) latest ON ttdh.id_hoa_don = latest.id_hoa_don AND ttdh.thoi_gian = latest.max_thoi_gian
                        WHERE ttdh.id_trang_thai_don_hang = 7 AND ttdh.deleted = 0
                    )
                    AND YEAR(hd.ngay_tao) = YEAR(GETDATE())
                    AND MONTH(hd.ngay_tao) = MONTH(GETDATE())
                    AND hd.deleted = 0
                    """;

            BigDecimal monthRevenue = jdbcTemplate.queryForObject(monthRevenueSQL, BigDecimal.class);
            dashboard.put("monthRevenue", monthRevenue);

            // Giá trị trung bình đơn hàng hoàn thành
            BigDecimal avgOrderValue = BigDecimal.ZERO;
            if (completedOrders != null && completedOrders > 0) {
                avgOrderValue = totalRevenue.divide(BigDecimal.valueOf(completedOrders), 2, java.math.RoundingMode.HALF_UP);
            }
            dashboard.put("avgOrderValue", avgOrderValue);
            return dashboard;

        } catch (Exception e) {
            throw new ApiException("Lỗi khi lấy dashboard thống kê: " + e.getMessage(), "500");
        }
    }

    /**
     * Thống kê theo khoảng thời gian cụ thể chỉ tính đơn hàng có trạng thái
     * CUỐI CÙNG là hoàn thành
     */
    public Map<String, Object> getCompletedOrderStatisticsByPeriod(String period, String startDate, String endDate) {
        try {
            StringBuilder sql = new StringBuilder();
            List<Object> params = new ArrayList<>();

            sql.append("SELECT ")
                    .append("COUNT(*) as so_don_hang, ")
                    .append("SUM(hd.tong_tien_sau_giam) as doanh_thu, ")
                    .append("AVG(hd.tong_tien_sau_giam) as doanh_thu_trung_binh ")
                    .append("FROM hoa_don hd ")
                    .append("WHERE hd.id IN ( ")
                    .append("    SELECT DISTINCT ttdh.id_hoa_don ")
                    .append("    FROM thong_tin_don_hang ttdh ")
                    .append("    INNER JOIN ( ")
                    .append("        SELECT id_hoa_don, MAX(thoi_gian) as max_thoi_gian ")
                    .append("        FROM thong_tin_don_hang ")
                    .append("        WHERE deleted = 0 ")
                    .append("        GROUP BY id_hoa_don ")
                    .append("    ) latest ON ttdh.id_hoa_don = latest.id_hoa_don AND ttdh.thoi_gian = latest.max_thoi_gian ")
                    .append("    WHERE ttdh.id_trang_thai_don_hang = 7 AND ttdh.deleted = 0 ")
                    .append(") ")
                    .append("AND hd.deleted = 0 ");

            // Xử lý các loại period khác nhau
            switch (period.toLowerCase()) {
                case "today":
                    sql.append("AND CONVERT(DATE, hd.ngay_tao) = CONVERT(DATE, GETDATE()) ");
                    break;
                case "week":
                    sql.append("AND hd.ngay_tao >= DATEADD(week, -1, GETDATE()) ");
                    break;
                case "month":
                    sql.append("AND YEAR(hd.ngay_tao) = YEAR(GETDATE()) ")
                            .append("AND MONTH(hd.ngay_tao) = MONTH(GETDATE()) ");
                    break;
                case "year":
                    sql.append("AND YEAR(hd.ngay_tao) = YEAR(GETDATE()) ");
                    break;
                case "custom":
                    if (startDate != null && !startDate.trim().isEmpty()) {
                        sql.append("AND hd.ngay_tao >= ? ");
                        params.add(startDate);
                    }
                    if (endDate != null && !endDate.trim().isEmpty()) {
                        sql.append("AND hd.ngay_tao <= ? ");
                        params.add(endDate);
                    }
                    break;
                default:
                    throw new ApiException("Period không hợp lệ: " + period, "400");
            }

            Map<String, Object> result = jdbcTemplate.queryForMap(sql.toString(), params.toArray());

            Map<String, Object> response = new HashMap<>();
            response.put("period", period);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("statistics", result);
            return response;

        } catch (Exception e) {
            throw new ApiException("Lỗi khi lấy thống kê theo period: " + e.getMessage(), "500");
        }
    }

    /**
     * So sánh doanh thu dự kiến vs thực tế
     */
    public Map<String, Object> getRevenueForecastComparison(String period, String startDate, String endDate) {
        try {
            Map<String, Object> comparison = new HashMap<>();

            // Lấy doanh thu thực tế
            Map<String, Object> actualRevenue = getActualRevenueByPeriod(period, startDate, endDate);

            // Lấy doanh thu dự kiến
            Map<String, Object> forecastRevenue = getForecastRevenueByPeriod(period, startDate, endDate);

            // Tính toán phần trăm hoàn thành
            BigDecimal actual = (BigDecimal) actualRevenue.get("totalRevenue");
            BigDecimal forecast = (BigDecimal) forecastRevenue.get("totalTarget");

            BigDecimal completionPercentage = BigDecimal.ZERO;
            BigDecimal difference = BigDecimal.ZERO;
            String status = "Chưa có mục tiêu";

            if (forecast != null && forecast.compareTo(BigDecimal.ZERO) > 0) {
                completionPercentage = actual.divide(forecast, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                difference = actual.subtract(forecast);

                if (completionPercentage.compareTo(BigDecimal.valueOf(100)) >= 0) {
                    status = "Đạt mục tiêu";
                } else if (completionPercentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
                    status = "Gần đạt mục tiêu";
                } else {
                    status = "Chưa đạt mục tiêu";
                }
            }

            comparison.put("period", period);
            comparison.put("startDate", startDate);
            comparison.put("endDate", endDate);
            comparison.put("actualRevenue", actual);
            comparison.put("forecastRevenue", forecast);
            comparison.put("completionPercentage", completionPercentage);
            comparison.put("difference", difference);
            comparison.put("status", status);
            comparison.put("actualData", actualRevenue);
            comparison.put("forecastData", forecastRevenue);
            return comparison;

        } catch (Exception e) {
            throw new ApiException("Lỗi khi so sánh doanh thu: " + e.getMessage(), "500");
        }
    }

    /**
     * Lấy doanh thu thực tế theo period
     */
    private Map<String, Object> getActualRevenueByPeriod(String period, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT ")
                .append("COUNT(*) as totalOrders, ")
                .append("COALESCE(SUM(hd.tong_tien_sau_giam), 0) as totalRevenue, ")
                .append("COALESCE(AVG(hd.tong_tien_sau_giam), 0) as avgOrderValue ")
                .append("FROM hoa_don hd ")
                .append("WHERE hd.id IN ( ")
                .append("    SELECT DISTINCT ttdh.id_hoa_don ")
                .append("    FROM thong_tin_don_hang ttdh ")
                .append("    INNER JOIN ( ")
                .append("        SELECT id_hoa_don, MAX(thoi_gian) as max_thoi_gian ")
                .append("        FROM thong_tin_don_hang ")
                .append("        WHERE deleted = 0 ")
                .append("        GROUP BY id_hoa_don ")
                .append("    ) latest ON ttdh.id_hoa_don = latest.id_hoa_don AND ttdh.thoi_gian = latest.max_thoi_gian ")
                .append("    WHERE ttdh.id_trang_thai_don_hang = 7 AND ttdh.deleted = 0 ")
                .append(") ")
                .append("AND hd.deleted = 0 ");

        // Thêm điều kiện thời gian dựa trên period
        addPeriodConditions(sql, params, period, startDate, endDate);

        Map<String, Object> result = jdbcTemplate.queryForMap(sql.toString(), params.toArray());
        result.put("type", "actual");
        return result;
    }

    /**
     * Lấy doanh thu dự kiến theo period
     */
    private Map<String, Object> getForecastRevenueByPeriod(String period, String startDate, String endDate) {
        // Tạo bảng tạm để lưu mục tiêu nếu chưa có
        createRevenueTargetTableIfNotExists();

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT ")
                .append("COALESCE(SUM(target_amount), 0) as totalTarget, ")
                .append("COUNT(*) as targetCount ")
                .append("FROM revenue_targets ")
                .append("WHERE deleted = 0 ");

        // Thêm điều kiện thời gian cho targets
        addTargetPeriodConditions(sql, params, period, startDate, endDate);

        Map<String, Object> result = jdbcTemplate.queryForMap(sql.toString(), params.toArray());
        result.put("type", "forecast");
        return result;
    }

    /**
     * Thêm điều kiện thời gian cho query
     */
    private void addPeriodConditions(StringBuilder sql, List<Object> params, String period, String startDate, String endDate) {
        switch (period.toLowerCase()) {
            case "today":
                sql.append("AND CONVERT(DATE, hd.ngay_tao) = CONVERT(DATE, GETDATE()) ");
                break;
            case "week":
                sql.append("AND hd.ngay_tao >= DATEADD(week, -1, GETDATE()) ");
                break;
            case "month":
                if (startDate != null && !startDate.trim().isEmpty()) {
                    sql.append("AND YEAR(hd.ngay_tao) = ? AND MONTH(hd.ngay_tao) = ? ");
                    String[] parts = startDate.split("-");
                    params.add(Integer.parseInt(parts[0])); // year
                    params.add(Integer.parseInt(parts[1])); // month
                } else {
                    sql.append("AND YEAR(hd.ngay_tao) = YEAR(GETDATE()) ")
                            .append("AND MONTH(hd.ngay_tao) = MONTH(GETDATE()) ");
                }
                break;
            case "quarter":
                sql.append("AND YEAR(hd.ngay_tao) = YEAR(GETDATE()) ")
                        .append("AND DATEPART(QUARTER, hd.ngay_tao) = DATEPART(QUARTER, GETDATE()) ");
                break;
            case "year":
                if (startDate != null && !startDate.trim().isEmpty()) {
                    sql.append("AND YEAR(hd.ngay_tao) = ? ");
                    params.add(Integer.parseInt(startDate));
                } else {
                    sql.append("AND YEAR(hd.ngay_tao) = YEAR(GETDATE()) ");
                }
                break;
            case "custom":
                if (startDate != null && !startDate.trim().isEmpty()) {
                    sql.append("AND hd.ngay_tao >= ? ");
                    params.add(startDate);
                }
                if (endDate != null && !endDate.trim().isEmpty()) {
                    sql.append("AND hd.ngay_tao <= ? ");
                    params.add(endDate);
                }
                break;
        }
    }

    /**
     * Thêm điều kiện thời gian cho targets
     */
    private void addTargetPeriodConditions(StringBuilder sql, List<Object> params, String period, String startDate, String endDate) {
        sql.append("AND period_type = ? ");
        params.add(period.toLowerCase());

        switch (period.toLowerCase()) {
            case "month":
                if (startDate != null && !startDate.trim().isEmpty()) {
                    sql.append("AND target_period = ? ");
                    params.add(startDate.substring(0, 7)); // YYYY-MM
                } else {
                    sql.append("AND target_period = FORMAT(GETDATE(), 'yyyy-MM') ");
                }
                break;
            case "quarter":
                if (startDate != null && !startDate.trim().isEmpty()) {
                    sql.append("AND target_period LIKE ? ");
                    params.add(startDate.substring(0, 4) + "-Q%");
                } else {
                    sql.append("AND target_period = CONCAT(YEAR(GETDATE()), '-Q', DATEPART(QUARTER, GETDATE())) ");
                }
                break;
            case "year":
                if (startDate != null && !startDate.trim().isEmpty()) {
                    sql.append("AND target_period = ? ");
                    params.add(startDate.substring(0, 4));
                } else {
                    sql.append("AND target_period = CAST(YEAR(GETDATE()) AS VARCHAR) ");
                }
                break;
        }
    }

    /**
     * Cập nhật mục tiêu doanh thu
     */
    public Map<String, Object> setRevenueTarget(String period, String targetDate, BigDecimal targetAmount) {
        try {
            createRevenueTargetTableIfNotExists();

            String targetPeriod = formatTargetPeriod(period, targetDate);

            // Kiểm tra xem đã có mục tiêu cho period này chưa
            String checkSql = "SELECT COUNT(*) FROM revenue_targets WHERE period_type = ? AND target_period = ? AND deleted = 0";
            Integer existingCount = jdbcTemplate.queryForObject(checkSql, Integer.class, period.toLowerCase(), targetPeriod);

            if (existingCount > 0) {
                // Cập nhật mục tiêu hiện có
                String updateSql = """
                        UPDATE revenue_targets 
                        SET target_amount = ?, updated_at = GETDATE() 
                        WHERE period_type = ? AND target_period = ? AND deleted = 0
                        """;
                jdbcTemplate.update(updateSql, targetAmount, period.toLowerCase(), targetPeriod);
            } else {
                // Tạo mục tiêu mới
                String insertSql = """
                        INSERT INTO revenue_targets (period_type, target_period, target_amount, created_at, updated_at, deleted)
                        VALUES (?, ?, ?, GETDATE(), GETDATE(), 0)
                        """;
                jdbcTemplate.update(insertSql, period.toLowerCase(), targetPeriod, targetAmount);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("period", period);
            result.put("targetPeriod", targetPeriod);
            result.put("targetAmount", targetAmount);
            result.put("action", existingCount > 0 ? "updated" : "created");

            return result;

        } catch (Exception e) {
            throw new ApiException("Lỗi khi cập nhật mục tiêu doanh thu: " + e.getMessage(), "500");
        }
    }

    /**
     * Lấy danh sách mục tiêu doanh thu
     */
    public Map<String, Object> getRevenueTargets(String period, String year) {
        try {
            createRevenueTargetTableIfNotExists();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT period_type, target_period, target_amount, created_at, updated_at ")
                    .append("FROM revenue_targets ")
                    .append("WHERE period_type = ? AND deleted = 0 ");

            List<Object> params = new ArrayList<>();
            params.add(period.toLowerCase());

            if (year != null && !year.trim().isEmpty()) {
                sql.append("AND target_period LIKE ? ");
                params.add(year + "%");
            }

            sql.append("ORDER BY target_period DESC");

            List<Map<String, Object>> targets = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            Map<String, Object> result = new HashMap<>();
            result.put("period", period);
            result.put("year", year);
            result.put("targets", targets);
            result.put("totalTargets", targets.size());

            return result;

        } catch (Exception e) {
            throw new ApiException("Lỗi khi lấy danh sách mục tiêu doanh thu: " + e.getMessage(), "500");
        }
    }

    /**
     * Tạo bảng revenue_targets nếu chưa có
     */
    private void createRevenueTargetTableIfNotExists() {
        try {
            String createTableSql = """
                    IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='revenue_targets' AND xtype='U')
                    CREATE TABLE revenue_targets (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        period_type VARCHAR(20) NOT NULL,
                        target_period VARCHAR(20) NOT NULL,
                        target_amount DECIMAL(15,2) NOT NULL,
                        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
                        updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
                        deleted BIT NOT NULL DEFAULT 0,
                        UNIQUE(period_type, target_period)
                    )
                    """;
            jdbcTemplate.execute(createTableSql);
        } catch (Exception e) {
            // Bảng có thể đã tồn tại, bỏ qua lỗi
        }
    }

    /**
     * Format target period theo định dạng chuẩn
     */
    private String formatTargetPeriod(String period, String targetDate) {
        switch (period.toLowerCase()) {
            case "month":
                return targetDate.length() >= 7 ? targetDate.substring(0, 7) : targetDate; // YYYY-MM
            case "quarter":
                if (targetDate.contains("Q")) {
                    return targetDate; // Already in YYYY-Q# format
                }
                // Convert month to quarter
                String[] parts = targetDate.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int quarter = (month - 1) / 3 + 1;
                return year + "-Q" + quarter;
            case "year":
                return targetDate.length() >= 4 ? targetDate.substring(0, 4) : targetDate; // YYYY
            default:
                return targetDate;
        }
    }

    /**
     * Gửi thông báo thay đổi địa chỉ giao hàng cho khách hàng Hỗ trợ cả khách
     * hàng đã đăng ký và khách lẻ
     */
    public void sendAddressChangeNotification(Integer orderId, AddressChangeNotificationRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("404", "Không tìm thấy đơn hàng"));
        BigDecimal originalTotal = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
        BigDecimal recordedRefundAmount = BigDecimal.ZERO;
        boolean customerPaidFullBeforeChange = false;
        BigDecimal updatedTotal = BigDecimal.ZERO;

        // Kiểm tra xem đơn hàng đã được thay đổi địa chỉ giao hàng trước đây chưa
        // Nếu có record ThongTinDonHang với idTrangThaiDonHang = 8 thì không cho phép thay đổi lần thứ 2
        boolean alreadyAddressChanged = thongTinDonHangRepository.existsByHoaDonIdAndStatusId(hoaDon.getId(), 8);
        // Lấy thông tin khách hàng - ưu tiên từ hoaDon (cho khách lẻ), sau đó từ idKhachHang
        String customerEmail = null;
        String customerName = "Khách hàng";

        // Thử lấy email từ hoaDon trước (cho khách lẻ)
        if (hoaDon.getEmailNguoiNhan() != null && !hoaDon.getEmailNguoiNhan().isEmpty()) {
            customerEmail = hoaDon.getEmailNguoiNhan();
        }

        // Lấy tên từ hoaDon (cho khách lẻ)
        if (hoaDon.getTenNguoiNhan() != null && !hoaDon.getTenNguoiNhan().isEmpty()) {
            customerName = hoaDon.getTenNguoiNhan();
        }

        // Fallback: Nếu không có email từ hoaDon, thử từ idKhachHang
        if ((customerEmail == null || customerEmail.isEmpty()) && hoaDon.getIdKhachHang() != null) {
            KhachHang khachHang = hoaDon.getIdKhachHang();
            if (khachHang.getEmail() != null && !khachHang.getEmail().isEmpty()) {
                customerEmail = khachHang.getEmail();
            }
            if ((customerName.equals("Khách hàng")) && khachHang.getTenKhachHang() != null) {
                customerName = khachHang.getTenKhachHang();
            }
        }

        // Nếu vẫn không có email, log warning nhưng vẫn tiếp tục
        if (customerEmail == null || customerEmail.isEmpty()) {
            customerEmail = "";
        }

        // Cập nhật phí phụ (hoặc ghi chú khi hoàn phí)
        if (request != null && request.getSurcharge() != null && request.getSurcharge().compareTo(BigDecimal.ZERO) != 0) {
            hoaDon.setPhuPhi(request.getSurcharge());
        }

        // Xử lý từ shippingFeeChange (nếu có - ưu tiên hơn surcharge)
        if (request != null && request.getShippingFeeChange() != null) {
            AddressChangeNotificationRequest.ShippingFeeChange feeChange = request.getShippingFeeChange();
            BigDecimal rawDifference = feeChange.getDifference() != null ? feeChange.getDifference() : BigDecimal.ZERO;
            Boolean isExtraProvided = feeChange.getIsExtra();
            boolean inferredExtra = Boolean.TRUE.equals(isExtraProvided);
            boolean inferredRefund = Boolean.FALSE.equals(isExtraProvided);

            if (!inferredExtra && !inferredRefund) {
                if (rawDifference.compareTo(BigDecimal.ZERO) > 0) {
                    inferredExtra = true;
                } else if (rawDifference.compareTo(BigDecimal.ZERO) < 0) {
                    inferredRefund = true;
                }
            }

            if (inferredExtra) {
                // === PHỤ PHÍ: Tăng phí ===
                BigDecimal extraFee = rawDifference.abs();
                if (extraFee.compareTo(BigDecimal.ZERO) > 0) {
                    hoaDon.setPhuPhi(extraFee);
                    hoaDon.setHoanPhi(BigDecimal.ZERO);

                    BigDecimal currentTotal = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
                    updatedTotal = currentTotal.add(extraFee);
                    hoaDon.setTongTien(updatedTotal);
                    hoaDon.setTongTienSauGiam(updatedTotal);

                    BigDecimal soTienDaThanhToan = hoaDon.getSoTienDaThanhToan() != null ? hoaDon.getSoTienDaThanhToan() : BigDecimal.ZERO;
                    BigDecimal soTienConLai = updatedTotal.subtract(soTienDaThanhToan);
                    hoaDon.setSoTienConLai(soTienConLai.compareTo(BigDecimal.ZERO) > 0 ? soTienConLai : BigDecimal.ZERO);

                }
            } else if (inferredRefund) {
                // === HOÀN PHÍ: Giảm phí ===
                BigDecimal refundFee = rawDifference.abs();
                if (refundFee.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal soTienDaThanhToan = hoaDon.getSoTienDaThanhToan() != null ? hoaDon.getSoTienDaThanhToan() : BigDecimal.ZERO;
                    BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;

                    boolean daTraDuTien = soTienDaThanhToan.compareTo(tongTien) >= 0;
                    recordedRefundAmount = refundFee;
                    customerPaidFullBeforeChange = daTraDuTien;

                    hoaDon.setPhuPhi(BigDecimal.ZERO);
                    hoaDon.setHoanPhi(refundFee);

                    if (daTraDuTien) {
                        BigDecimal currentTotal = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
                        hoaDon.setTongTienSauGiam(currentTotal);
                        hoaDon.setSoTienConLai(BigDecimal.ZERO);
                    } else {
                        BigDecimal newTotal = tongTien.subtract(refundFee);
                        if (newTotal.compareTo(BigDecimal.ZERO) < 0) {
                            newTotal = BigDecimal.ZERO;
                        }
                        hoaDon.setTongTien(newTotal);
                        hoaDon.setTongTienSauGiam(newTotal);

                        BigDecimal soTienConLai = newTotal.subtract(soTienDaThanhToan);
                        hoaDon.setSoTienConLai(soTienConLai.compareTo(BigDecimal.ZERO) > 0 ? soTienConLai : BigDecimal.ZERO);
                    }
                }
            }
        }

        // Lưu thay đổi phí phụ/hoàn phí và tổng tiền
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // Cập nhật trạng thái đơn hàng thành id = 8 (Thay đổi địa chỉ giao hàng)
        try {
            TrangThaiDonHang trangThaiAddressChange = trangThaiDonHangRepository.findById(8)
                    .orElse(null);

            if (trangThaiAddressChange != null) {
                // Tạo bản ghi ThongTinDonHang mới với trạng thái 8
                ThongTinDonHang thongTinDonHang = new ThongTinDonHang();
                thongTinDonHang.setIdHoaDon(hoaDon);
                thongTinDonHang.setIdTrangThaiDonHang(trangThaiAddressChange);
                thongTinDonHang.setTrangThai(true);
                thongTinDonHang.setThoiGian(LocalDateTime.now());
                thongTinDonHang.setDeleted(false);
                thongTinDonHangRepository.save(thongTinDonHang);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating order status to id = 8", e);
        }

        // Xử lý thông báo về phụ phí/hoàn phí cho khách hàng
        AddressChangeNotificationRequest.ShippingFeeChange feeChange = request != null ? request.getShippingFeeChange() : null;
        handleSurchargeRefundNotification(
                savedHoaDon,
                feeChange,
                customerEmail,
                customerName,
                recordedRefundAmount,
                customerPaidFullBeforeChange,
                originalTotal,
                savedHoaDon.getTongTien() != null ? savedHoaDon.getTongTien() : BigDecimal.ZERO);

        BigDecimal loggedSurcharge = (request != null && request.getSurcharge() != null)
                ? request.getSurcharge()
                : BigDecimal.ZERO;
    }

    /**
     * Xử lý thông báo về phụ phí/hoàn phí cho khách hàng - Phụ phí < 40k: Gửi quà hiện vật
     * - Phụ phí >= 40k: Tạo voucher tương ứng - Hoàn phí: Gửi thông báo hoàn
     * lại
     */
    private void handleSurchargeRefundNotification(HoaDon hoaDon,
            AddressChangeNotificationRequest.ShippingFeeChange feeChange, String customerEmail, String customerName,
            BigDecimal recordedRefundAmount, boolean customerPaidFullBeforeChange, BigDecimal originalTotal, BigDecimal updatedTotal) {
        if (feeChange == null || feeChange.getDifference() == null
                || feeChange.getDifference().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal rawDifference = feeChange.getDifference() != null ? feeChange.getDifference() : BigDecimal.ZERO;
        if (rawDifference.compareTo(BigDecimal.ZERO) == 0 && recordedRefundAmount != null
                && recordedRefundAmount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        Boolean isExtraProvided = feeChange.getIsExtra();
        boolean inferredExtra = Boolean.TRUE.equals(isExtraProvided);
        boolean inferredRefund = Boolean.FALSE.equals(isExtraProvided);

        if (!inferredExtra && !inferredRefund) {
            if (rawDifference.compareTo(BigDecimal.ZERO) > 0) {
                inferredExtra = true;
            } else if (rawDifference.compareTo(BigDecimal.ZERO) < 0) {
                inferredRefund = true;
            }
        }

        boolean hasCustomerEmail = customerEmail != null && !customerEmail.isEmpty();

        if (inferredExtra) {
            // === PHỤ PHÍ: Tăng phí ===
            BigDecimal surcharge = rawDifference.abs();

            if (surcharge.compareTo(new BigDecimal("40000")) < 0) {
                // Phụ phí < 40k: Gửi quà hiện vật
                if (hasCustomerEmail) {
                    emailService.sendAddressChangeNotificationEmail(
                            customerEmail, customerName, hoaDon.getMaHoaDon(),
                            null, null, surcharge
                    );
                }
            } else {
                // Phụ phí >= 40k: Tạo voucher tương ứng
                String voucherCode = generateVoucherCode(hoaDon.getId(), "SURC");

                try {
                    // Tạo PhieuGiamGia mới cho voucher
                    createSurchargeVoucher(hoaDon, surcharge, voucherCode, customerName);

                } catch (Exception e) {
                    throw new RuntimeException("Failed to create voucher for surcharge", e);
                }

                // Gửi email với thông báo về voucher
                if (hasCustomerEmail) {
                    emailService.sendAddressChangeNotificationEmail(
                            customerEmail, customerName, hoaDon.getMaHoaDon(),
                            null, null, surcharge
                    );
                }
            }
        } else if (inferredRefund) {
            // === HOÀN PHÍ: Giảm phí ===
            BigDecimal refund = (recordedRefundAmount != null && recordedRefundAmount.compareTo(BigDecimal.ZERO) > 0)
                    ? recordedRefundAmount
                    : rawDifference.abs();

            // Store original and updated totals from parameters
            BigDecimal storedOriginalTotal = originalTotal != null ? originalTotal : BigDecimal.ZERO;
            BigDecimal storedUpdatedTotal = updatedTotal != null ? updatedTotal : hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;

            if (refund == null || refund.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }

            boolean appliedDirectlyToOrder = !customerPaidFullBeforeChange;
            boolean rewardAsGift = false;
            boolean rewardAsVoucher = false;
            String voucherCode = null;
            LocalDateTime voucherExpiry = null;

            if (customerPaidFullBeforeChange) {
                if (refund.compareTo(REFUND_VOUCHER_THRESHOLD) < 0) {
                    rewardAsGift = true;
                } else {
                    rewardAsVoucher = true;
                    VoucherInfo voucherInfo = createRefundVoucher(hoaDon, refund, customerName);
                    voucherCode = voucherInfo.code();
                    voucherExpiry = voucherInfo.expiry();
                }
            }

            if (hasCustomerEmail) {
                RefundNotificationEmailData emailData = RefundNotificationEmailData.builder()
                        .customerName(customerName)
                        .customerEmail(customerEmail)
                        .orderCode(hoaDon.getMaHoaDon())
                        .refundAmount(refund)
                        .appliedToOrder(appliedDirectlyToOrder)
                        .originalTotal(storedOriginalTotal)
                        .newTotal(storedUpdatedTotal)
                        .giftReward(rewardAsGift)
                        .voucherReward(rewardAsVoucher)
                        .voucherCode(voucherCode)
                        .voucherExpiry(voucherExpiry)
                        .build();

                emailService.sendAddressChangeRefundNotificationEmail(emailData);
            }
        }
    }

    /**
     * Tạo voucher cho phụ phí >= 40k
     */
    private void createSurchargeVoucher(HoaDon hoaDon, BigDecimal voucherValue,
            String voucherCode, String customerName) {
        try {
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiry = issuedAt.plusMonths(3);

            VoucherInfo info = persistVoucherForOrder(
                    hoaDon,
                    voucherValue,
                    voucherCode,
                    "Voucher Bù Phụ Phí - " + customerName,
                    "Voucher bù phụ phí do thay đổi địa chỉ giao hàng",
                    issuedAt,
                    expiry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create voucher for surcharge", e);
        }
    }

    private VoucherInfo createRefundVoucher(HoaDon hoaDon, BigDecimal voucherValue, String customerName) {
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiry = issuedAt.plusMonths(3);
        String voucherCode = generateVoucherCode(hoaDon.getId(), "REF");

        return persistVoucherForOrder(
                hoaDon,
                voucherValue,
                voucherCode,
                "Voucher Hoàn Phí - " + customerName,
                "Voucher hoàn phí phát sinh do thay đổi địa chỉ giao hàng",
                issuedAt,
                expiry);
    }

    private VoucherInfo persistVoucherForOrder(HoaDon hoaDon,
            BigDecimal voucherValue,
            String voucherCode,
            String voucherName,
            String description,
            LocalDateTime issuedAt,
            LocalDateTime expiry) {

        PhieuGiamGia voucher = new PhieuGiamGia();
        voucher.setMaPhieuGiamGia(voucherCode);
        voucher.setTenPhieuGiamGia(voucherName);
        voucher.setLoaiPhieuGiamGia(true);
        voucher.setGiaTriGiamGia(voucherValue);
        voucher.setHoaDonToiThieu(BigDecimal.ZERO);
        voucher.setSoLuongDung(1);
        voucher.setNgayBatDau(issuedAt);
        voucher.setNgayKetThuc(expiry);
        voucher.setTrangThai(true);
        voucher.setDeleted(false);
        voucher.setFeatured(hoaDon.getIdKhachHang() != null);
        voucher.setMoTa(description);
        voucher.setCreatedAt(issuedAt);
        voucher.setUpdatedAt(issuedAt);

        PhieuGiamGia savedVoucher = phieuGiamGiaRepository.save(voucher);

        if (hoaDon.getIdKhachHang() != null) {
            PhieuGiamGiaCaNhan personalVoucher = new PhieuGiamGiaCaNhan();
            personalVoucher.setIdKhachHang(hoaDon.getIdKhachHang());
            personalVoucher.setIdPhieuGiamGia(savedVoucher);
            personalVoucher.setTenPhieuGiamGiaCaNhan(voucherName);
            personalVoucher.setNgayNhan(issuedAt);
            personalVoucher.setNgayHetHan(expiry);
            personalVoucher.setTrangThai(true);
            personalVoucher.setDeleted(false);
            phieuGiamGiaCaNhanRepository.save(personalVoucher);
        }
        return new VoucherInfo(voucherCode, issuedAt, expiry);
    }

    private static final class VoucherInfo {

        private final String code;
        private final LocalDateTime issuedAt;
        private final LocalDateTime expiry;

        private VoucherInfo(String code, LocalDateTime issuedAt, LocalDateTime expiry) {
            this.code = code;
            this.issuedAt = issuedAt;
            this.expiry = expiry;
        }

        private String code() {
            return code;
        }

        private LocalDateTime issuedAt() {
            return issuedAt;
        }

        private LocalDateTime expiry() {
            return expiry;
        }
    }

    /**
     * Tạo mã voucher duy nhất
     */
    private String generateVoucherCode(Integer hoaDonId, String prefix) {
        String normalizedPrefix = (prefix != null && !prefix.isBlank()) ? prefix.toUpperCase() : "VCH";
        return String.format("%s_%d_%d", normalizedPrefix, hoaDonId, System.currentTimeMillis() % 100000);
    }

    /**
     * Ghép các phần của địa chỉ thành chuỗi hoàn chỉnh
     */
    @SuppressWarnings("unused")
    private String buildAddressString(AddressChangeNotificationRequest.AddressInfo address) {
        if (address == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        if (address.getDiaChiCuThe() != null && !address.getDiaChiCuThe().isEmpty()) {
            sb.append(address.getDiaChiCuThe());
        }

        if (address.getPhuong() != null && !address.getPhuong().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(address.getPhuong());
        }

        if (address.getQuan() != null && !address.getQuan().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(address.getQuan());
        }

        if (address.getThanhPho() != null && !address.getThanhPho().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(address.getThanhPho());
        }

        return sb.toString();
    }
}
