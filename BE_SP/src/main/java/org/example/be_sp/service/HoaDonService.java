package org.example.be_sp.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.be_sp.entity.ChiTietSanPham;
import org.example.be_sp.entity.HinhThucThanhToan;
import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.HoaDonChiTiet;
import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.entity.PhuongThucThanhToan;
import org.example.be_sp.entity.ThongTinDonHang;
import org.example.be_sp.entity.TimelineDonHang;
import org.example.be_sp.entity.TrangThaiDonHang;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.email.OrderEmailData;
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
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HoaDonService {

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

    public List<HoaDonResponse> getAll() {
        return hoaDonRepository.findAll().stream().map(HoaDonResponse::new).toList();
    }

    public PagingResponse<HoaDonResponse> phanTrang(Integer no, Integer size) {
        Pageable page = PageRequest.of(no, size);
        return new PagingResponse<>(hoaDonRepository.findAll(page).map(HoaDonResponse::new), no);
    }

    public HoaDonResponse getByid(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id).orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn", "404"));
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

        // Tự động điền tên và mã phiếu giảm giá
        if (hd.getIdPhieuGiamGia() != null) {
            if (request.getTenPhieuGiamGia() == null || request.getTenPhieuGiamGia().trim().isEmpty()) {
                hd.setTenPhieuGiamGia(hd.getIdPhieuGiamGia().getTenPhieuGiamGia());
            }
            if (request.getMaPhieuGiamGia() == null || request.getMaPhieuGiamGia().trim().isEmpty()) {
                hd.setMaPhieuGiamGia(hd.getIdPhieuGiamGia().getMaPhieuGiamGia());
            }
        }

        // Map diaChiNhanHang từ request vào diaChiNguoiNhan của entity
        // (vì tên field khác nhau nên ModelMapper không tự động map)
        if (request.getDiaChiNhanHang() != null && !request.getDiaChiNhanHang().trim().isEmpty()) {
            hd.setDiaChiNguoiNhan(request.getDiaChiNhanHang());
        }

        // Set ngayTao if not provided
        if (hd.getNgayTao() == null) {
            hd.setNgayTao(LocalDate.now());
        }
        
        // Set createAt if not provided
        if (hd.getCreateAt() == null) {
            hd.setCreateAt(LocalDate.now());
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
                        log.warn("Skipping HoaDonChiTiet: missing idChiTietSanPham/idBienTheSanPham/idBienThe");
                        continue;
                    }
                    
                    final Integer idChiTietSanPham = idChiTietSanPhamValue;
                    ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(idChiTietSanPham)
                        .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết sản phẩm với id: " + idChiTietSanPham, "404"));
                    
                    hoaDonChiTiet.setIdChiTietSanPham(chiTietSanPham);
                    hoaDonChiTiet.setSoLuong(chiTietRequest.getSoLuong() != null ? chiTietRequest.getSoLuong() : 1);
                    hoaDonChiTiet.setGiaBan(chiTietRequest.getGiaBan() != null ? chiTietRequest.getGiaBan() : BigDecimal.ZERO);
                    
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
                    log.info("Created HoaDonChiTiet for product variant ID: {}, quantity: {}", idChiTietSanPham, hoaDonChiTiet.getSoLuong());
                }
                
                // Update tongTien after creating all chi tiết
                savedHoaDon = hoaDonRepository.findById(savedHoaDon.getId()).orElseThrow();
                BigDecimal totalTien = savedHoaDon.getHoaDonChiTiets().stream()
                    .filter(ct -> ct.getDeleted() == null || !ct.getDeleted())
                    .map(ct -> ct.getThanhTien() != null ? ct.getThanhTien() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                if (savedHoaDon.getTongTien() == null || savedHoaDon.getTongTien().compareTo(BigDecimal.ZERO) == 0) {
                    savedHoaDon.setTongTien(totalTien);
                    if (savedHoaDon.getTongTienSauGiam() == null || savedHoaDon.getTongTienSauGiam().compareTo(BigDecimal.ZERO) == 0) {
                        savedHoaDon.setTongTienSauGiam(totalTien);
                    }
                    savedHoaDon = hoaDonRepository.save(savedHoaDon);
                }
                
                log.info("Created {} HoaDonChiTiet items for order ID: {}", request.getHoaDonChiTiet().size(), savedHoaDon.getId());
            } catch (Exception e) {
                log.error("Failed to create HoaDonChiTiet for order ID: {}", savedHoaDon.getId(), e);
                // Don't throw - order is already created, chi tiết can be added later
            }
        }

        // Generate invoice code using stored procedure
        String generatedMaHoaDon = null;
        try {
            generatedMaHoaDon = generateInvoiceCode(savedHoaDon.getId());
            // Refresh entity to get updated ma_hoa_don and load HoaDonChiTiets from database
            savedHoaDon = hoaDonRepository.findById(savedHoaDon.getId()).orElseThrow();
            // Force load HoaDonChiTiets collection (LAZY loading)
            if (savedHoaDon.getHoaDonChiTiets() != null) {
                savedHoaDon.getHoaDonChiTiets().size(); // Trigger lazy loading
            }
            log.info("Generated invoice code: {} for order ID: {}", generatedMaHoaDon, savedHoaDon.getId());
        } catch (Exception e) {
            log.error("Failed to generate invoice code for order ID: {}", savedHoaDon.getId(), e);
            // Fallback: generate temporary code if stored procedure fails
            if (savedHoaDon.getMaHoaDon() == null || savedHoaDon.getMaHoaDon().trim().isEmpty()) {
                String tempCode = "HD" + String.format("%010d", savedHoaDon.getId());
                savedHoaDon.setMaHoaDon(tempCode);
                savedHoaDon = hoaDonRepository.save(savedHoaDon);
                log.warn("Using temporary invoice code: {} for order ID: {}", tempCode, savedHoaDon.getId());
            }
        }

        // Ensure maHoaDon is set before creating response
        if (savedHoaDon.getMaHoaDon() == null || savedHoaDon.getMaHoaDon().trim().isEmpty()) {
            String fallbackCode = generatedMaHoaDon != null ? generatedMaHoaDon : ("HD" + String.format("%010d", savedHoaDon.getId()));
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
                log.info("Created TimelineDonHang for order ID: {}", savedHoaDon.getId());
            } else {
                log.warn("No staff member found, skipping TimelineDonHang creation for order ID: {}", savedHoaDon.getId());
            }
        } catch (Exception e) {
            log.error("Failed to create TimelineDonHang for order ID: {}", savedHoaDon.getId(), e);
            // Don't throw - order creation should still succeed
        }

        // Create HinhThucThanhToan if payment method is provided
        if (request.getIdPhuongThucThanhToan() != null) {
            try {
                PhuongThucThanhToan phuongThucThanhToan = phuongThucThanhToanRepository.findById(request.getIdPhuongThucThanhToan())
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
                    log.info("Created HinhThucThanhToan for order ID: {} with payment method ID: {} (COD: {})", 
                        savedHoaDon.getId(), request.getIdPhuongThucThanhToan(), isCOD);
                }
            } catch (Exception e) {
                log.error("Failed to create HinhThucThanhToan for order ID: {}", savedHoaDon.getId(), e);
                // Don't throw - order creation should still succeed
            }
        }

        // Create ThongTinDonHang for tracking order status
        try {
            // Find default status "Chờ xác nhận" (usually ID 1 or first status)
            TrangThaiDonHang defaultStatus = trangThaiDonHangRepository.findAll().stream()
                .filter(tt -> tt.getTenTrangThaiDonHang() != null && 
                    (tt.getTenTrangThaiDonHang().contains("Chờ xác nhận") || 
                     tt.getTenTrangThaiDonHang().contains("Chờ") ||
                     tt.getId() == 1))
                .findFirst()
                .orElse(trangThaiDonHangRepository.findAll().stream().findFirst().orElse(null));
            
            if (defaultStatus != null) {
                ThongTinDonHang thongTinDonHang = new ThongTinDonHang();
                thongTinDonHang.setIdHoaDon(savedHoaDon);
                thongTinDonHang.setIdTrangThaiDonHang(defaultStatus);
                thongTinDonHang.setThoiGian(LocalDate.now());
                thongTinDonHang.setGhiChu("Đơn hàng được tạo từ hệ thống bán hàng online");
                thongTinDonHang.setTrangThai(true);
                thongTinDonHang.setDeleted(false);
                thongTinDonHangRepository.save(thongTinDonHang);
                log.info("Created ThongTinDonHang for order ID: {}", savedHoaDon.getId());
            } else {
                log.warn("No TrangThaiDonHang found, skipping ThongTinDonHang creation for order ID: {}", savedHoaDon.getId());
            }
        } catch (Exception e) {
            log.error("Failed to create ThongTinDonHang for order ID: {}", savedHoaDon.getId(), e);
            // Don't throw - order creation should still succeed
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
            log.warn("Failed to force load lazy collections for order ID: {}", savedHoaDon.getId(), e);
            // Continue - response will still be created
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
                        "Đơn hàng mới từ " + (savedHoaDon.getTenNguoiNhan() != null ? savedHoaDon.getTenNguoiNhan() : "khách hàng"),
                        2 // in progress
                );
            }
        } catch (Exception e) {
            log.error("Failed to send order creation notification: {}", e.getMessage());
        }

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
            hd.setGiaoHang(request.getLoaiDon());   // ✅ update loại đơn
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

            // Tự động điền tên và mã phiếu giảm giá nếu chưa có
            if (hd.getIdPhieuGiamGia() != null) {
                if (hd.getTenPhieuGiamGia() == null || hd.getTenPhieuGiamGia().trim().isEmpty()) {
                    hd.setTenPhieuGiamGia(hd.getIdPhieuGiamGia().getTenPhieuGiamGia());
                }
                if (hd.getMaPhieuGiamGia() == null || hd.getMaPhieuGiamGia().trim().isEmpty()) {
                    hd.setMaPhieuGiamGia(hd.getIdPhieuGiamGia().getMaPhieuGiamGia());
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
        hd.setUpdateAt(LocalDate.now());

        // Track original status before update
        Boolean originalStatus = hd.getTrangThai();

        HoaDon saved = hoaDonRepository.save(hd);

        //NOTIFICATION: Order status updated
        if (request.getTrangThai() != null && !request.getTrangThai().equals(originalStatus) && saved.getIdKhachHang() != null) {
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
                log.error("Failed to send order update notification: {}", e.getMessage());
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
                log.warn("Order {} has no email address, skipping order confirmation email",
                        hoaDon.getMaHoaDon());
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
                    .orderDate(hoaDon.getNgayTao() != null ? hoaDon.getNgayTao() : LocalDate.now())
                    .totalAmount(hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO)
                    .discountAmount(discountAmount)
                    .shippingFee(hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO)
                    .finalAmount(hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : BigDecimal.ZERO)
                    .deliveryAddress(hoaDon.getDiaChiNguoiNhan() != null ? hoaDon.getDiaChiNguoiNhan() : "")
                    .phoneNumber(hoaDon.getSoDienThoaiNguoiNhan() != null ? hoaDon.getSoDienThoaiNguoiNhan() : "")
                    .items(items)
                    .build();

            emailService.sendOrderConfirmationEmail(emailData);
            log.info("Order confirmation email sent for order: {}", hoaDon.getMaHoaDon());

        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order: {}",
                    hoaDon.getMaHoaDon(), e);
            // Don't throw exception - we don't want to rollback the order creation
        }
    }

    /**
     * Thêm dữ liệu mẫu cho 3 hóa đơn
     */
    public void addSampleData() {
        try {
            log.info("Bắt đầu thêm dữ liệu mẫu...");

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
                hoaDon.setNgayTao(LocalDate.now());
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

                log.info("Đã tạo hóa đơn mẫu {} với ID: {}", i, savedHoaDon.getId());
            }

            log.info("Hoàn thành thêm dữ liệu mẫu!");

        } catch (Exception e) {
            log.error("Lỗi khi thêm dữ liệu mẫu: {}", e.getMessage(), e);
            throw new ApiException("Lỗi khi thêm dữ liệu mẫu: " + e.getMessage(), "500");
        }
    }

    /**
     * Helper method to send order confirmation email
     */
}
