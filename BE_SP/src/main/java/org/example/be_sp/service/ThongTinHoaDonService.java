package org.example.be_sp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

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
        return thongTinDonHangRepository.findById(id).map(ThongTinDonHangResponse::new).orElseThrow(() -> new ApiException("Không tìm thấy thông tin đơn hàng", "404"));
    }

    public void add(ThongTinHoaDonRequest thongTinHoaDonRequest) {
        ThongTinDonHang ttdh = MapperUtils.map(thongTinHoaDonRequest, ThongTinDonHang.class);
        HoaDon hoaDon = hoaDonService.findById(thongTinHoaDonRequest.getIdHoaDon())
                .orElseThrow(() -> new ApiException("Hóa đơn không tồn tại", "404"));
        ttdh.setIdHoaDon(hoaDon);
        ttdh.setIdTrangThaiDonHang(trangThaiDonHangService.findById(thongTinHoaDonRequest.getIdTrangThaiDonHang())
                .orElseThrow(() -> new ApiException("Trạng thái đơn hàng không tồn tại", "404")));

        // Đảm bảo thời gian được set đúng
        if (ttdh.getThoiGian() == null) {
            ttdh.setThoiGian(java.time.LocalDateTime.now());
        }
        ttdh.setTrangThai(true);
        ttdh.setDeleted(false);

        ThongTinDonHang saved = thongTinDonHangRepository.save(ttdh);

        // Send order status update email
        sendOrderStatusUpdateEmail(saved);
    }

    public void update(Integer id, ThongTinHoaDonRequest thongTinHoaDonRequest) {
        ThongTinDonHang ttdh = thongTinDonHangRepository.findById(id).orElseThrow(() -> new ApiException("Không tìm thấy thông tin đơn hàng", "404"));
        MapperUtils.mapToExisting(thongTinHoaDonRequest, ttdh);
        ttdh.setIdHoaDon(hoaDonService.findById(thongTinHoaDonRequest.getIdHoaDon())
                .orElseThrow(() -> new ApiException("Hóa đơn không tồn tại", "404")));
        ttdh.setIdTrangThaiDonHang(trangThaiDonHangService.findById(thongTinHoaDonRequest.getIdTrangThaiDonHang())
                .orElseThrow(() -> new ApiException("Trạng thái đơn hàng không tồn tại", "404")));
        thongTinDonHangRepository.save(ttdh);
    }

    public void delete(Integer id) {
        ThongTinDonHang ttdh = thongTinDonHangRepository.findById(id).orElseThrow(() -> new ApiException("Không tìm thấy thông tin đơn hàng", "404"));
        ttdh.setDeleted(true);
        thongTinDonHangRepository.save(ttdh);
    }

    /**
     * Lấy thông tin đơn hàng theo ID hóa đơn
     */
    @Transactional(readOnly = true)
    public List<ThongTinDonHangResponse> getByHoaDonId(Integer hoaDonId) {
        try {
            List<ThongTinDonHang> results = thongTinDonHangRepository.findByHoaDonId(hoaDonId);
            if (results == null || results.isEmpty()) {
                log.warn("Không tìm thấy thông tin đơn hàng cho hóa đơn ID: {}", hoaDonId);
                return new ArrayList<>();
            }
            return results.stream()
                    .map(ThongTinDonHangResponse::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin đơn hàng cho hóa đơn ID: {}", hoaDonId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Lấy thông tin đơn hàng mới nhất theo ID hóa đơn
     */
    @Transactional(readOnly = true)
    public ThongTinDonHangResponse getLatestByHoaDonId(Integer hoaDonId) {
        try {
            // Thử sử dụng method không có Pageable trước
            List<ThongTinDonHang> results = thongTinDonHangRepository.findByHoaDonIdOrderByThoiGianDesc(hoaDonId);

            if (results == null || results.isEmpty()) {
                log.warn("Không tìm thấy thông tin đơn hàng cho hóa đơn ID: {}", hoaDonId);
                return null;
            }

            ThongTinDonHang thongTinDonHang = results.get(0); // Lấy record đầu tiên (mới nhất)

            // Force load các quan hệ cần thiết
            if (thongTinDonHang.getIdHoaDon() != null) {
                Hibernate.initialize(thongTinDonHang.getIdHoaDon());

                // Load quan hệ của HoaDon
                if (thongTinDonHang.getIdHoaDon().getIdKhachHang() != null) {
                    Hibernate.initialize(thongTinDonHang.getIdHoaDon().getIdKhachHang());
                }
                if (thongTinDonHang.getIdHoaDon().getIdNhanVien() != null) {
                    Hibernate.initialize(thongTinDonHang.getIdHoaDon().getIdNhanVien());
                }
                if (thongTinDonHang.getIdHoaDon().getHoaDonChiTiets() != null) {
                    Hibernate.initialize(thongTinDonHang.getIdHoaDon().getHoaDonChiTiets());

                    // Load quan hệ của HoaDonChiTiet
                    for (var hdct : thongTinDonHang.getIdHoaDon().getHoaDonChiTiets()) {
                        if (hdct.getIdChiTietSanPham() != null) {
                            Hibernate.initialize(hdct.getIdChiTietSanPham());

                            // Load quan hệ của ChiTietSanPham
                            if (hdct.getIdChiTietSanPham().getIdSanPham() != null) {
                                Hibernate.initialize(hdct.getIdChiTietSanPham().getIdSanPham());
                            }
                            if (hdct.getIdChiTietSanPham().getIdMauSac() != null) {
                                Hibernate.initialize(hdct.getIdChiTietSanPham().getIdMauSac());
                            }
                            if (hdct.getIdChiTietSanPham().getIdKichThuoc() != null) {
                                Hibernate.initialize(hdct.getIdChiTietSanPham().getIdKichThuoc());
                            }
                            if (hdct.getIdChiTietSanPham().getIdDeGiay() != null) {
                                Hibernate.initialize(hdct.getIdChiTietSanPham().getIdDeGiay());
                            }
                            if (hdct.getIdChiTietSanPham().getIdChatLieu() != null) {
                                Hibernate.initialize(hdct.getIdChiTietSanPham().getIdChatLieu());
                            }
                            if (hdct.getIdChiTietSanPham().getIdTrongLuong() != null) {
                                Hibernate.initialize(hdct.getIdChiTietSanPham().getIdTrongLuong());
                            }

                            // Load quan hệ ảnh sản phẩm
                            if (hdct.getIdChiTietSanPham().getChiTietSanPhamAnhs() != null) {
                                Hibernate.initialize(hdct.getIdChiTietSanPham().getChiTietSanPhamAnhs());

                                // Load quan hệ của ChiTietSanPhamAnh
                                for (var ctspa : hdct.getIdChiTietSanPham().getChiTietSanPhamAnhs()) {
                                    if (ctspa.getIdAnhSanPham() != null) {
                                        Hibernate.initialize(ctspa.getIdAnhSanPham());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (thongTinDonHang.getIdTrangThaiDonHang() != null) {
                Hibernate.initialize(thongTinDonHang.getIdTrangThaiDonHang());
            }

            return new ThongTinDonHangResponse(thongTinDonHang);
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin đơn hàng mới nhất cho hóa đơn ID: {}", hoaDonId, e);
            return null;
        }
    }

    /**
     * sssssss Lấy danh sách sản phẩm đã bán theo ID hóa đơn
     */
    @Transactional(readOnly = true)
    public List<org.example.be_sp.model.response.SanPhamDaBanResponse> getSanPhamDaBanByHoaDonId(Integer hoaDonId) {
        try {
            // Lấy hóa đơn từ repository
            var hoaDonOpt = hoaDonService.findById(hoaDonId);
            if (hoaDonOpt.isEmpty()) {
                log.warn("Không tìm thấy hóa đơn với ID: {}", hoaDonId);
                return new ArrayList<>();
            }
            var hoaDon = hoaDonOpt.get();

            // Force load hóa đơn chi tiết
            if (hoaDon.getHoaDonChiTiets() != null) {
                Hibernate.initialize(hoaDon.getHoaDonChiTiets());

                // Load quan hệ của HoaDonChiTiet
                for (var hdct : hoaDon.getHoaDonChiTiets()) {
                    if (hdct.getIdChiTietSanPham() != null) {
                        Hibernate.initialize(hdct.getIdChiTietSanPham());

                        // Load quan hệ của ChiTietSanPham
                        if (hdct.getIdChiTietSanPham().getIdSanPham() != null) {
                            Hibernate.initialize(hdct.getIdChiTietSanPham().getIdSanPham());
                        }
                        if (hdct.getIdChiTietSanPham().getIdMauSac() != null) {
                            Hibernate.initialize(hdct.getIdChiTietSanPham().getIdMauSac());
                        }
                        if (hdct.getIdChiTietSanPham().getIdKichThuoc() != null) {
                            Hibernate.initialize(hdct.getIdChiTietSanPham().getIdKichThuoc());
                        }
                        if (hdct.getIdChiTietSanPham().getIdDeGiay() != null) {
                            Hibernate.initialize(hdct.getIdChiTietSanPham().getIdDeGiay());
                        }
                        if (hdct.getIdChiTietSanPham().getIdChatLieu() != null) {
                            Hibernate.initialize(hdct.getIdChiTietSanPham().getIdChatLieu());
                        }
                        if (hdct.getIdChiTietSanPham().getIdTrongLuong() != null) {
                            Hibernate.initialize(hdct.getIdChiTietSanPham().getIdTrongLuong());
                        }

                        // Load quan hệ ảnh sản phẩm
                        if (hdct.getIdChiTietSanPham().getChiTietSanPhamAnhs() != null) {
                            Hibernate.initialize(hdct.getIdChiTietSanPham().getChiTietSanPhamAnhs());

                            // Load quan hệ của ChiTietSanPhamAnh
                            for (var ctspa : hdct.getIdChiTietSanPham().getChiTietSanPhamAnhs()) {
                                if (ctspa.getIdAnhSanPham() != null) {
                                    Hibernate.initialize(ctspa.getIdAnhSanPham());
                                }
                            }
                        }
                    }
                }

                return hoaDon.getHoaDonChiTiets().stream()
                        .filter(item -> !item.getDeleted())
                        .map(org.example.be_sp.model.response.SanPhamDaBanResponse::new)
                        .collect(Collectors.toList());
            }

            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sản phẩm đã bán cho hóa đơn ID: {}", hoaDonId, e);
            return new ArrayList<>();
        }
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
                    .orderId(hoaDon.getId())
                    .orderCode(hoaDon.getMaHoaDon())
                    .customerName(hoaDon.getTenNguoiNhan() != null ? hoaDon.getTenNguoiNhan() : "Khách hàng")
                    .customerEmail(customerEmail)
                    .orderDate(hoaDon.getCreateAt() != null ? hoaDon.getCreateAt() : LocalDateTime.now())
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
