package org.example.be_sp.service;

import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.ThongTinDonHang;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.email.OrderEmailData;
import org.example.be_sp.model.request.ThongTinHoaDonRequest;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.model.response.ThongTinDonHangResponse;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.repository.ThongTinDonHangRepository;
import org.example.be_sp.repository.TrangThaiDonHangRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ThongTinHoaDonService {
    @Autowired
    ThongTinDonHangRepository thongTinDonHangRepository;
    @Autowired
    HoaDonRepository hoaDonService;
    @Autowired
    TrangThaiDonHangRepository trangThaiDonHangService;
    @Autowired
    private EmailService emailService;
    public List<ThongTinDonHangResponse> getAll() {
        return thongTinDonHangRepository.findAll().stream().map(ThongTinDonHangResponse::new).toList();
    }
    public PagingResponse<ThongTinDonHangResponse> phanTrang(Integer no, Integer size) {
        Pageable page = PageRequest.of(no, size);
        return new PagingResponse<>(thongTinDonHangRepository.findAll(page).map(ThongTinDonHangResponse::new), no);
    }
    public ThongTinDonHangResponse getById(Integer id) {
        return thongTinDonHangRepository.findById(id).map(ThongTinDonHangResponse::new).orElseThrow(()-> new ApiException("Không tìm thấy thông tin đơn hàng","404" ));
    }
    public void add(ThongTinHoaDonRequest thongTinHoaDonRequest) {
        ThongTinDonHang ttdh = MapperUtils.map(thongTinHoaDonRequest, ThongTinDonHang.class);
        HoaDon hoaDon = hoaDonService.getById(thongTinHoaDonRequest.getIdHoaDon());
        ttdh.setIdHoaDon(hoaDon);
        ttdh.setIdTrangThaiDonHang(trangThaiDonHangService.getById(thongTinHoaDonRequest.getIdTrangThaiDonHang()));
        
        // Đảm bảo thời gian được set đúng
        if (ttdh.getThoiGian() == null) {
            ttdh.setThoiGian(java.time.LocalDate.now());
        }
        
        ThongTinDonHang saved = thongTinDonHangRepository.save(ttdh);
        
        // Send order status update email
        sendOrderStatusUpdateEmail(saved);
    }
    public void update(Integer id, ThongTinHoaDonRequest thongTinHoaDonRequest) {
        ThongTinDonHang ttdh = thongTinDonHangRepository.findById(id).orElseThrow(()-> new ApiException("Không tìm thấy thông tin đơn hàng","404" ));
        MapperUtils.mapToExisting(thongTinHoaDonRequest,ttdh);
        ttdh.setIdHoaDon(hoaDonService.getById(thongTinHoaDonRequest.getIdHoaDon()));
        ttdh.setIdTrangThaiDonHang(trangThaiDonHangService.getById(thongTinHoaDonRequest.getIdTrangThaiDonHang()));
        thongTinDonHangRepository.save(ttdh);
    }
    public void delete(Integer id) {
        ThongTinDonHang ttdh = thongTinDonHangRepository.findById(id).orElseThrow(()-> new ApiException("Không tìm thấy thông tin đơn hàng","404" ));
        ttdh.setDeleted(true);
        thongTinDonHangRepository.save(ttdh);
    }
    
    /**
     * Lấy thông tin đơn hàng theo ID hóa đơn
     */
    public List<ThongTinDonHangResponse> getByHoaDonId(Integer hoaDonId) {
        return thongTinDonHangRepository.findByHoaDonId(hoaDonId)
            .stream()
            .map(ThongTinDonHangResponse::new)
            .toList();
    }
    
    /**
     * Lấy thông tin đơn hàng mới nhất theo ID hóa đơn
     */
    public ThongTinDonHangResponse getLatestByHoaDonId(Integer hoaDonId) {
        return thongTinDonHangRepository.findLatestByHoaDonId(hoaDonId)
            .map(ThongTinDonHangResponse::new)
            .orElse(null);
    }
    
    /**
     * Helper method to send order status update email
     */
    private void sendOrderStatusUpdateEmail(ThongTinDonHang thongTinDonHang) {
        try {
            HoaDon hoaDon = thongTinDonHang.getIdHoaDon();
            if (hoaDon == null) {
                log.warn("ThongTinDonHang {} has no associated HoaDon", thongTinDonHang.getId());
                return;
            }
            
            // Get customer email - prefer order email, fallback to customer email
            String customerEmail = hoaDon.getEmailNguoiNhan();
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                if (hoaDon.getIdKhachHang() != null && hoaDon.getIdKhachHang().getEmail() != null) {
                    customerEmail = hoaDon.getIdKhachHang().getEmail();
                }
            }
            
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                log.warn("Order {} has no email address, skipping order status update email", 
                        hoaDon.getMaHoaDon());
                return;
            }
            
            // Get order status name
            String statusName = "Cập nhật đơn hàng";
            if (thongTinDonHang.getIdTrangThaiDonHang() != null) {
                statusName = thongTinDonHang.getIdTrangThaiDonHang().getTenTrangThaiDonHang();
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
                        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity.longValue()));
                        
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
                .orderStatus(statusName)
                .totalAmount(hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO)
                .discountAmount(discountAmount)
                .shippingFee(hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO)
                .finalAmount(hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : BigDecimal.ZERO)
                .deliveryAddress(hoaDon.getDiaChiNguoiNhan() != null ? hoaDon.getDiaChiNguoiNhan() : "")
                .phoneNumber(hoaDon.getSoDienThoaiNguoiNhan() != null ? hoaDon.getSoDienThoaiNguoiNhan() : "")
                .items(items)
                .build();
                
            emailService.sendOrderStatusUpdateEmail(emailData);
            log.info("Order status update email sent for order: {} - Status: {}", 
                    hoaDon.getMaHoaDon(), statusName);
            
        } catch (Exception e) {
            log.error("Failed to send order status update email for ThongTinDonHang: {}", 
                    thongTinDonHang.getId(), e);
            // Don't throw exception - we don't want to rollback the status update
        }
    }
}
