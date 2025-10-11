package org.example.be_sp.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.be_sp.entity.ChiTietSanPham;
import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.HoaDonChiTiet;
import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.email.OrderEmailData;
import org.example.be_sp.model.request.BanHangTaiQuayRequest;
import org.example.be_sp.model.response.HoaDonResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.ChiTietSanPhamRepository;
import org.example.be_sp.repository.HoaDonChiTietRepository;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.repository.NhanVienRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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



    public List<HoaDonResponse> getAll() {
        return hoaDonRepository.findAll().stream().map(HoaDonResponse::new).toList();
    }
    public PagingResponse<HoaDonResponse> phanTrang(Integer no, Integer size) {
        Pageable page = PageRequest.of(no, size);
        return new PagingResponse<>(hoaDonRepository.findAll(page).map(HoaDonResponse::new), no);
    }

    public HoaDonResponse getByid(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id).orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn","404"));
        return new HoaDonResponse(hoaDon);
    }
    public void add(BanHangTaiQuayRequest request) {
        HoaDon hd = MapperUtils.map(request, HoaDon.class);
        hd.setIdKhachHang(khachHangRepository.findKhachHangById(request.getIdKhachHang()));
        hd.setIdPhieuGiamGia(phieuGiamGiaService.getById(request.getIdPhieuGiamGia()));
        hd.setIdNhanVien(nhanVienRepository.getById(request.getIdNhanVien()));
        HoaDon savedHoaDon = hoaDonRepository.save(hd);
        
        // Send order confirmation email
        sendOrderConfirmationEmail(savedHoaDon);
    }
    public HoaDonResponse update(Integer id, BanHangTaiQuayRequest request) {
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn","404"));

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
            hd.setLoaiDon(request.getLoaiDon());   // ✅ update loại đơn
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

        // Gán lại các quan hệ
        if (request.getIdKhachHang() != null) {
            hd.setIdKhachHang(khachHangRepository.findKhachHangById(request.getIdKhachHang()));
        }
        if (request.getIdPhieuGiamGia() != null) {
            hd.setIdPhieuGiamGia(phieuGiamGiaService.getById(request.getIdPhieuGiamGia()));
        }
        if (request.getIdNhanVien() != null) {
            hd.setIdNhanVien(nhanVienRepository.getById(request.getIdNhanVien()));
        }

        hd.setUpdateAt(LocalDate.now());

        HoaDon saved = hoaDonRepository.save(hd);
        return new HoaDonResponse(saved);
    }



    public void delete(Integer id) {
        HoaDon hd = hoaDonRepository.findById(id).orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn","404"));
        hd.setDeleted(true);
        hoaDonRepository.save(hd);
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
                hoaDon.setLoaiDon(false); // Tại quầy
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
}
