package org.example.be_sp.service;

import java.util.ArrayList;
import java.util.List;

import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.entity.PhieuGiamGia;
import org.example.be_sp.entity.PhieuGiamGiaCaNhan;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.email.VoucherEmailData;
import org.example.be_sp.model.request.PhieuGiamGiaRequest;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.model.response.PhieuGiamGiaResponse;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.repository.PhieuGiamGiaCaNhanRepository;
import org.example.be_sp.repository.PhieuGiamGiaRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PhieuGiamGiaService {
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    private PhieuGiamGiaCaNhanRepository phieuGiamGiaCaNhanRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private EmailService emailService;

    public List<PhieuGiamGiaResponse> getAll() {
        return new ArrayList<>(phieuGiamGiaRepository.findAll().stream().map(PhieuGiamGiaResponse::new).toList());
    }

    public PhieuGiamGia getById(Integer id) {
        return phieuGiamGiaRepository.getById(id);
    }

    public PhieuGiamGiaResponse getByIdResponse(Integer id) {
        return phieuGiamGiaRepository.findById(id).map(PhieuGiamGiaResponse::new)
                .orElseThrow(() -> new ApiException("PhieuGiamGia not found", "404"));
    }

    public PagingResponse<PhieuGiamGiaResponse> paging(Integer page, Integer size) {
        return new PagingResponse<>(
                phieuGiamGiaRepository.findAll(PageRequest.of(page, size)).map(PhieuGiamGiaResponse::new), page);
    }

    public void add(PhieuGiamGiaRequest request) {
        PhieuGiamGia pgg = MapperUtils.map(request, PhieuGiamGia.class);
        PhieuGiamGia savedPgg = phieuGiamGiaRepository.save(pgg);
        if (request.getIdKhachHang() != null) {
            for (Integer idKhachHang : request.getIdKhachHang()) {
                PhieuGiamGiaCaNhan pggcn = new PhieuGiamGiaCaNhan();
                KhachHang khachHang = khachHangRepository.getById(idKhachHang);
                pggcn.setIdKhachHang(khachHang);
                pggcn.setIdPhieuGiamGia(savedPgg);
                pggcn.setTenPhieuGiamGiaCaNhan(request.getTenPhieuGiamGia());
                pggcn.setNgayNhan(request.getNgayBatDau());
                pggcn.setNgayHetHan(request.getNgayKetThuc());
                pggcn.setTrangThai(true);
                pggcn.setDeleted(false);
                PhieuGiamGiaCaNhan savedPggcn = phieuGiamGiaCaNhanRepository.save(pggcn);
                
                // Send voucher assignment email
                sendVoucherEmail(savedPggcn, khachHang, savedPgg);
            }
        }
    }

    public void update(Integer id, PhieuGiamGiaRequest request) {
        // Fetch existing entity to preserve generated fields
        PhieuGiamGia existingPgg = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ApiException("PhieuGiamGia not found", "404"));

        // Map request to new entity
        PhieuGiamGia updatedPgg = MapperUtils.map(request, PhieuGiamGia.class);
        updatedPgg.setId(id);

        // Preserve the generated maPhieuGiamGia field
        updatedPgg.setMaPhieuGiamGia(existingPgg.getMaPhieuGiamGia());

        PhieuGiamGia saved = phieuGiamGiaRepository.save(updatedPgg);

        List<PhieuGiamGiaCaNhan> existingPersonalCoupons = phieuGiamGiaCaNhanRepository.findByIdPhieuGiamGiaId(id);
        for (PhieuGiamGiaCaNhan existing : existingPersonalCoupons) {
            existing.setDeleted(true);
            phieuGiamGiaCaNhanRepository.save(existing);
        }

        // Create new personal coupons if specified
        if (request.getIdKhachHang() != null && !request.getIdKhachHang().isEmpty()) {
            for (Integer idKhachHang : request.getIdKhachHang()) {
                PhieuGiamGiaCaNhan pggcn = new PhieuGiamGiaCaNhan();
                pggcn.setIdKhachHang(khachHangRepository.getById(idKhachHang));
                pggcn.setIdPhieuGiamGia(saved);
                pggcn.setTenPhieuGiamGiaCaNhan(request.getTenPhieuGiamGia());
                pggcn.setNgayNhan(request.getNgayBatDau());
                pggcn.setNgayHetHan(request.getNgayKetThuc());
                pggcn.setTrangThai(true);
                pggcn.setDeleted(false);
                PhieuGiamGiaCaNhan savedPggcn = phieuGiamGiaCaNhanRepository.save(pggcn);
                
                // Send voucher assignment email
                KhachHang customer = khachHangRepository.getById(idKhachHang);
                sendVoucherEmail(savedPggcn, customer, saved);
            }
        }
    }
    
    /**
     * Helper method to send voucher assignment email
     */
    private void sendVoucherEmail(PhieuGiamGiaCaNhan pggcn, KhachHang khachHang, PhieuGiamGia pgg) {
        try {
            if (khachHang.getEmail() != null && !khachHang.getEmail().trim().isEmpty()) {
                VoucherEmailData emailData = VoucherEmailData.builder()
                    .customerName(khachHang.getTenKhachHang())
                    .customerEmail(khachHang.getEmail())
                    .voucherCode(pggcn.getMaPhieuGiamGiaCaNhan())
                    .voucherName(pgg.getTenPhieuGiamGia())
                    .voucherType(pgg.getLoaiPhieuGiamGia() ? "PERCENTAGE" : "FIXED_AMOUNT")
                    .discountValue(pgg.getGiaTriGiamGia())
                    .maxDiscount(pgg.getSoTienToiDa())
                    .minOrderValue(pgg.getHoaDonToiThieu())
                    .validFrom(pgg.getNgayBatDau())
                    .validUntil(pgg.getNgayKetThuc())
                    .usageLimit(pgg.getSoLuongDung())
                    .description(pgg.getMoTa())
                    .build();
                    
                emailService.sendVoucherAssignmentEmail(emailData);
                log.info("Voucher assignment email sent to customer: {} for voucher: {}", 
                        khachHang.getEmail(), pggcn.getMaPhieuGiamGiaCaNhan());
            } else {
                log.warn("Customer {} has no email address, skipping voucher email", khachHang.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send voucher assignment email to customer: {}", 
                    khachHang.getId(), e);
            // Don't throw exception - we don't want to rollback the voucher creation
        }
    }
    
    public void updateStatus(Integer id) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.getById(id);
        phieuGiamGia.setTrangThai(!phieuGiamGia.getTrangThai()); // Toggle trangThai status (active/inactive)
        phieuGiamGiaRepository.save(phieuGiamGia);

        // Also update personal coupons if they exist
        if (phieuGiamGia.getPhieuGiamGiaCaNhans() != null && !phieuGiamGia.getPhieuGiamGiaCaNhans().isEmpty()) {
            for (PhieuGiamGiaCaNhan pggcn : phieuGiamGia.getPhieuGiamGiaCaNhans()) {
                pggcn.setTrangThai(phieuGiamGia.getTrangThai()); // Update trangThai to match parent coupon
                phieuGiamGiaCaNhanRepository.save(pggcn);
            }
        }
    }

    public void delete(Integer id) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ApiException("PhieuGiamGia not found", "404"));

        // Soft delete the coupon
        phieuGiamGia.setDeleted(true);
        phieuGiamGiaRepository.save(phieuGiamGia);

        // Also soft delete associated personal coupons
        List<PhieuGiamGiaCaNhan> personalCoupons = phieuGiamGiaCaNhanRepository.findByIdPhieuGiamGiaId(id);
        for (PhieuGiamGiaCaNhan pggcn : personalCoupons) {
            pggcn.setDeleted(true);
            phieuGiamGiaCaNhanRepository.save(pggcn);
        }
    }

    public List<PhieuGiamGiaResponse> getActiveCouponsForCustomer(Integer idKhachHang) {
        KhachHang khachHang = khachHangRepository.findById(idKhachHang)
                .orElseThrow(() -> new ApiException("KhachHang not found", "404"));
        List<PhieuGiamGia> activeCoupons = phieuGiamGiaRepository
                .findAllByDeletedFalseAndTrangThaiTrueAndLoaiPhieuGiamGiaTrue(false, true, true);
        List<PhieuGiamGiaCaNhan> personalCoupons = phieuGiamGiaCaNhanRepository
                .findAllByIdKhachHangAndDeletedAndTrangThai(khachHang, false, true);
        List<PhieuGiamGiaResponse> result = new ArrayList<>();
        for (PhieuGiamGia coupon : activeCoupons) {
            result.add(new PhieuGiamGiaResponse(coupon));
        }
        for (PhieuGiamGiaCaNhan personalCoupon : personalCoupons) {
            result.add(new PhieuGiamGiaResponse(personalCoupon.getIdPhieuGiamGia()));
        }
        return result;
    }
}
