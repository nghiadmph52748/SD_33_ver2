package org.example.be_sp.service;

import java.util.ArrayList;
import java.util.List;

import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.entity.PhieuGiamGia;
import org.example.be_sp.entity.PhieuGiamGiaCaNhan;
import org.example.be_sp.entity.PhieuGiamGiaHistory;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.email.VoucherEmailData;
import org.example.be_sp.model.request.PhieuGiamGiaRequest;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.model.response.PhieuGiamGiaResponse;
import org.example.be_sp.model.response.PhieuGiamGiaHistoryResponse;
import org.example.be_sp.repository.ChiTietPhieuGiamGiaRepository;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.repository.PhieuGiamGiaHistoryRepository;
import org.example.be_sp.repository.PhieuGiamGiaCaNhanRepository;
import org.example.be_sp.repository.PhieuGiamGiaRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;

@Service
@Slf4j
public class PhieuGiamGiaService {
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    private PhieuGiamGiaCaNhanRepository phieuGiamGiaCaNhanRepository;
    @Autowired
    private ChiTietPhieuGiamGiaRepository chiTietPhieuGiamGiaRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PhieuGiamGiaHistoryRepository historyRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public List<PhieuGiamGiaResponse> getAll() {
        return new ArrayList<>(phieuGiamGiaRepository.findAll().stream()
                .map(PhieuGiamGiaResponse::new)
                .toList());
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
                phieuGiamGiaRepository.findAll(PageRequest.of(page, size))
                        .map(PhieuGiamGiaResponse::new), page);
    }

    public void add(PhieuGiamGiaRequest request) {
        PhieuGiamGia pgg = MapperUtils.map(request, PhieuGiamGia.class);
        pgg.setCreatedAt(LocalDateTime.now());
        pgg.setUpdatedAt(LocalDateTime.now());
        PhieuGiamGia savedPgg = phieuGiamGiaRepository.save(pgg);
        
        // Log creation
        logHistory(savedPgg.getId(), "TẠO MỚI", "Tạo phiếu giảm giá: " + savedPgg.getTenPhieuGiamGia());
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

        // Create a snapshot of old values for change tracking
        // We need to do this BEFORE mapping the new values to avoid JPA cache issues
        PhieuGiamGia oldSnapshot = new PhieuGiamGia();
        oldSnapshot.setTenPhieuGiamGia(existingPgg.getTenPhieuGiamGia());
        oldSnapshot.setGiaTriGiamGia(existingPgg.getGiaTriGiamGia());
        oldSnapshot.setLoaiPhieuGiamGia(existingPgg.getLoaiPhieuGiamGia());
        oldSnapshot.setSoTienToiDa(existingPgg.getSoTienToiDa());
        oldSnapshot.setHoaDonToiThieu(existingPgg.getHoaDonToiThieu());
        oldSnapshot.setSoLuongDung(existingPgg.getSoLuongDung());
        oldSnapshot.setNgayBatDau(existingPgg.getNgayBatDau());
        oldSnapshot.setNgayKetThuc(existingPgg.getNgayKetThuc());
        oldSnapshot.setTrangThai(existingPgg.getTrangThai());
        oldSnapshot.setMoTa(existingPgg.getMoTa());
        oldSnapshot.setFeatured(existingPgg.getFeatured());
        
        // Snapshot old customer IDs for featured coupons
        List<Integer> oldCustomerIds = phieuGiamGiaCaNhanRepository.findByIdPhieuGiamGiaId(id).stream()
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .map(p -> p.getIdKhachHang().getId())
                .sorted()
                .toList();
        
        // Snapshot old product IDs
        List<Integer> oldProductIds = existingPgg.getChiTietPhieuGiamGias().stream()
                .map(ct -> ct.getIdChiTietSanPham().getId())
                .sorted()
                .toList();

        // Map request to new entity
        PhieuGiamGia updatedPgg = MapperUtils.map(request, PhieuGiamGia.class);
        updatedPgg.setId(id);

        // Preserve the generated maPhieuGiamGia field
        updatedPgg.setMaPhieuGiamGia(existingPgg.getMaPhieuGiamGia());
        
        // Preserve createdAt and update updatedAt
        updatedPgg.setCreatedAt(existingPgg.getCreatedAt());
        updatedPgg.setUpdatedAt(LocalDateTime.now());

        PhieuGiamGia saved = phieuGiamGiaRepository.save(updatedPgg);

        // Update personal coupons and track changes
        List<PhieuGiamGiaCaNhan> existingPersonalCoupons = phieuGiamGiaCaNhanRepository.findByIdPhieuGiamGiaId(id);
        for (PhieuGiamGiaCaNhan existing : existingPersonalCoupons) {
            existing.setDeleted(true);
            phieuGiamGiaCaNhanRepository.save(existing);
        }

        // Create new personal coupons if specified
        List<Integer> newCustomerIds = new java.util.ArrayList<>();
        if (request.getIdKhachHang() != null && !request.getIdKhachHang().isEmpty()) {
            newCustomerIds.addAll(request.getIdKhachHang());
            newCustomerIds.sort(Integer::compareTo);
            
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
        
        // Build complete change description including basic fields, customers, and products
        StringBuilder allChanges = new StringBuilder();
        
        // Basic field changes
        String basicChanges = buildChangeDescription(oldSnapshot, updatedPgg);
        if (!"Không có thay đổi".equals(basicChanges)) {
            allChanges.append(basicChanges);
        }
        
        // Customer changes (for featured coupons)
        String customerChanges = buildCustomerChanges(oldCustomerIds, newCustomerIds);
        if (!customerChanges.isEmpty()) {
            if (allChanges.length() > 0) allChanges.append("\n");
            allChanges.append(customerChanges);
        }
        
        // Note: Product changes will be tracked separately by ChiTietPhieuGiamGiaController
        // We don't track it here because products are updated in a separate API call
        
        String finalChanges = allChanges.length() > 0 ? allChanges.toString() : "Không có thay đổi";
        
        // Use lyDoThayDoi from request if provided, prepend it to the auto-generated changes
        String description = (request.getLyDoThayDoi() != null && !request.getLyDoThayDoi().trim().isEmpty()) 
                ? request.getLyDoThayDoi() + "\n\n" + finalChanges
                : finalChanges;
        
        // Log history after save with all changes
        logHistory(id, "CẬP NHẬT", description);
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
                    .voucherType(pgg.getLoaiPhieuGiamGia() ? "FIXED_AMOUNT" : "PERCENTAGE")
                    .discountValue(pgg.getGiaTriGiamGia())
                    .maxDiscount(pgg.getSoTienToiDa())
                    .minOrderValue(pgg.getHoaDonToiThieu())
                    .validFrom(pgg.getNgayBatDau())
                    .validUntil(pgg.getNgayKetThuc())
                    .usageLimit(pgg.getSoLuongDung())
                    .description(pgg.getMoTa())
                    .build();
                    
                // Send email asynchronously - this returns immediately
                emailService.sendVoucherAssignmentEmail(emailData);
                log.info("Voucher assignment email queued for customer: {} (voucher: {}). Email will be sent in background.", 
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

    @Transactional
    public void delete(Integer id) {
        // Verify the entity exists before deleting
        if (!phieuGiamGiaRepository.existsById(id)) {
            throw new ApiException("PhieuGiamGia not found", "404");
        }
        
        PhieuGiamGia pgg = phieuGiamGiaRepository.getById(id);
        String couponName = pgg.getTenPhieuGiamGia();
        
        // Log deletion before actually deleting
        logHistory(id, "XÓA", "Xóa phiếu giảm giá: " + couponName);
        
        // Flush and clear to ensure history is saved
        entityManager.flush();
        entityManager.clear();

        // Delete associated records in order (to maintain referential integrity)
        // 1. Delete chi_tiet_phieu_giam_gia (product associations)
        chiTietPhieuGiamGiaRepository.deleteByIdPhieuGiamGia(id);
        entityManager.flush();
        
        // 2. Delete phieu_giam_gia_ca_nhan (personal coupons)
        List<PhieuGiamGiaCaNhan> personalCoupons = phieuGiamGiaCaNhanRepository.findByIdPhieuGiamGiaId(id);
        for (PhieuGiamGiaCaNhan pggcn : personalCoupons) {
            phieuGiamGiaCaNhanRepository.deleteById(pggcn.getId());
        }
        entityManager.flush();
        
        // 3. Delete phieu_giam_gia_history (history records)
        historyRepository.hardDeleteByIdPhieuGiamGia(id);
        entityManager.flush();

        // 4. Finally delete the coupon itself
        phieuGiamGiaRepository.deleteById(id);
        entityManager.flush();
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
    
    /**
     * Get change history for a coupon
     */
    public List<PhieuGiamGiaHistoryResponse> getHistory(Integer idPhieuGiamGia) {
        List<PhieuGiamGiaHistory> history = historyRepository.findByIdPhieuGiamGiaOrderByNgayThayDoiDesc(idPhieuGiamGia);
        return history.stream()
                .map(PhieuGiamGiaHistoryResponse::fromEntity)
                .toList();
    }
    
    /**
     * Log a history entry for a coupon change
     */
    private void logHistory(Integer idPhieuGiamGia, String action, String description) {
        try {
            Integer userId = getCurrentUserId();
            if (userId == null) {
                log.warn("Cannot log history: No authenticated user found");
                return;
            }
            
            PhieuGiamGiaHistory history = PhieuGiamGiaHistory.builder()
                    .idPhieuGiamGia(idPhieuGiamGia)
                    .idNhanVien(userId)
                    .hanhDong(action)
                    .moTaThayDoi(description)
                    .ngayThayDoi(LocalDateTime.now())
                    .build();
            
            historyRepository.save(history);
            log.info("Logged history for coupon {} - Action: {}", idPhieuGiamGia, action);
        } catch (Exception e) {
            log.error("Failed to log history for coupon {}", idPhieuGiamGia, e);
            // Don't throw - history logging should not break main flow
        }
    }
    
    /**
     * Build a description of changes between old and new coupon
     */
    private String buildChangeDescription(PhieuGiamGia oldPgg, PhieuGiamGia newPgg) {
        StringBuilder changes = new StringBuilder();
        
        log.debug("[CHANGE_TRACKING] Comparing old vs new:");
        log.debug("[CHANGE_TRACKING] Old - Ten: {}, GiaTri: {}, SoLuong: {}", 
                oldPgg.getTenPhieuGiamGia(), oldPgg.getGiaTriGiamGia(), oldPgg.getSoLuongDung());
        log.debug("[CHANGE_TRACKING] New - Ten: {}, GiaTri: {}, SoLuong: {}", 
                newPgg.getTenPhieuGiamGia(), newPgg.getGiaTriGiamGia(), newPgg.getSoLuongDung());
        
        // Tên phiếu
        if (!safeEquals(oldPgg.getTenPhieuGiamGia(), newPgg.getTenPhieuGiamGia())) {
            changes.append(String.format("Tên: '%s' → '%s'\n", 
                    oldPgg.getTenPhieuGiamGia(), newPgg.getTenPhieuGiamGia()));
        }
        
        // Giá trị giảm
        if (!safeEquals(oldPgg.getGiaTriGiamGia(), newPgg.getGiaTriGiamGia())) {
            changes.append(String.format("Giá trị giảm: %s → %s\n", 
                    formatNumber(oldPgg.getGiaTriGiamGia()), formatNumber(newPgg.getGiaTriGiamGia())));
        }
        
        // Loại phiếu
        if (!safeEquals(oldPgg.getLoaiPhieuGiamGia(), newPgg.getLoaiPhieuGiamGia())) {
            String oldType = Boolean.TRUE.equals(oldPgg.getLoaiPhieuGiamGia()) ? "Số tiền" : "Phần trăm";
            String newType = Boolean.TRUE.equals(newPgg.getLoaiPhieuGiamGia()) ? "Số tiền" : "Phần trăm";
            changes.append(String.format("Loại giảm giá: %s → %s\n", oldType, newType));
        }
        
        // Số tiền tối đa
        if (!safeEquals(oldPgg.getSoTienToiDa(), newPgg.getSoTienToiDa())) {
            changes.append(String.format("Giảm tối đa: %s → %s\n", 
                    formatNumber(oldPgg.getSoTienToiDa()), formatNumber(newPgg.getSoTienToiDa())));
        }
        
        // Hóa đơn tối thiểu
        if (!safeEquals(oldPgg.getHoaDonToiThieu(), newPgg.getHoaDonToiThieu())) {
            changes.append(String.format("Đơn hàng tối thiểu: %s → %s\n", 
                    formatNumber(oldPgg.getHoaDonToiThieu()), formatNumber(newPgg.getHoaDonToiThieu())));
        }
        
        // Số lượng dùng
        log.debug("[CHANGE_TRACKING] Comparing SoLuongDung: old={} (type={}), new={} (type={}), equals={}",
                oldPgg.getSoLuongDung(), 
                oldPgg.getSoLuongDung() != null ? oldPgg.getSoLuongDung().getClass().getName() : "null",
                newPgg.getSoLuongDung(),
                newPgg.getSoLuongDung() != null ? newPgg.getSoLuongDung().getClass().getName() : "null",
                safeEquals(oldPgg.getSoLuongDung(), newPgg.getSoLuongDung()));
        
        if (!safeEquals(oldPgg.getSoLuongDung(), newPgg.getSoLuongDung())) {
            changes.append(String.format("Số lượng: %s → %s\n", 
                    oldPgg.getSoLuongDung(), newPgg.getSoLuongDung()));
            log.debug("[CHANGE_TRACKING] Added change for SoLuongDung");
        }
        
        // Ngày bắt đầu
        if (!safeEquals(oldPgg.getNgayBatDau(), newPgg.getNgayBatDau())) {
            changes.append(String.format("Ngày bắt đầu: %s → %s\n", 
                    formatDateTime(oldPgg.getNgayBatDau()), formatDateTime(newPgg.getNgayBatDau())));
        }
        
        // Ngày kết thúc
        if (!safeEquals(oldPgg.getNgayKetThuc(), newPgg.getNgayKetThuc())) {
            changes.append(String.format("Ngày kết thúc: %s → %s\n", 
                    formatDateTime(oldPgg.getNgayKetThuc()), formatDateTime(newPgg.getNgayKetThuc())));
        }
        
        // Trạng thái
        if (!safeEquals(oldPgg.getTrangThai(), newPgg.getTrangThai())) {
            String oldStatus = Boolean.TRUE.equals(oldPgg.getTrangThai()) ? "Hoạt động" : "Tạm dừng";
            String newStatus = Boolean.TRUE.equals(newPgg.getTrangThai()) ? "Hoạt động" : "Tạm dừng";
            changes.append(String.format("Trạng thái: %s → %s\n", oldStatus, newStatus));
        }
        
        // Mô tả
        if (!safeEquals(oldPgg.getMoTa(), newPgg.getMoTa())) {
            String oldDesc = oldPgg.getMoTa() != null && !oldPgg.getMoTa().isEmpty() ? oldPgg.getMoTa() : "(Không có)";
            String newDesc = newPgg.getMoTa() != null && !newPgg.getMoTa().isEmpty() ? newPgg.getMoTa() : "(Không có)";
            changes.append(String.format("Mô tả: %s → %s\n", oldDesc, newDesc));
        }
        
        // Nổi bật
        if (!safeEquals(oldPgg.getFeatured(), newPgg.getFeatured())) {
            String oldFeatured = Boolean.TRUE.equals(oldPgg.getFeatured()) ? "Có" : "Không";
            String newFeatured = Boolean.TRUE.equals(newPgg.getFeatured()) ? "Có" : "Không";
            changes.append(String.format("Nổi bật: %s → %s\n", oldFeatured, newFeatured));
        }
        
        String result = changes.length() > 0 ? changes.toString().trim() : "Không có thay đổi";
        log.debug("[CHANGE_TRACKING] Final changes description: {}", result);
        return result;
    }
    
    /**
     * Build customer changes description
     */
    private String buildCustomerChanges(List<Integer> oldCustomerIds, List<Integer> newCustomerIds) {
        if (oldCustomerIds.equals(newCustomerIds)) {
            return "";
        }
        
        StringBuilder changes = new StringBuilder();
        
        // Find added customers
        List<Integer> added = newCustomerIds.stream()
                .filter(id -> !oldCustomerIds.contains(id))
                .toList();
        
        // Find removed customers
        List<Integer> removed = oldCustomerIds.stream()
                .filter(id -> !newCustomerIds.contains(id))
                .toList();
        
        if (!added.isEmpty() || !removed.isEmpty()) {
            changes.append("Khách hàng áp dụng: ");
            
            if (oldCustomerIds.isEmpty() && !newCustomerIds.isEmpty()) {
                changes.append(String.format("Thêm %d khách hàng", newCustomerIds.size()));
            } else if (!oldCustomerIds.isEmpty() && newCustomerIds.isEmpty()) {
                changes.append(String.format("Xóa tất cả %d khách hàng", oldCustomerIds.size()));
            } else {
                List<String> changeParts = new java.util.ArrayList<>();
                if (!added.isEmpty()) {
                    changeParts.add(String.format("Thêm %d", added.size()));
                }
                if (!removed.isEmpty()) {
                    changeParts.add(String.format("Xóa %d", removed.size()));
                }
                changes.append(String.join(", ", changeParts));
                changes.append(String.format(" (%d → %d khách hàng)", oldCustomerIds.size(), newCustomerIds.size()));
            }
        }
        
        return changes.toString();
    }
    
    /**
     * Format number for display
     */
    private String formatNumber(Object value) {
        if (value == null) return "(Không có)";
        return value.toString();
    }
    
    /**
     * Format datetime for display
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "(Không có)";
        return dateTime.toString();
    }
    
    /**
     * Safe equals for nullable objects
     * Special handling for BigDecimal to use compareTo instead of equals
     */
    private boolean safeEquals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;
        if (obj1 == null || obj2 == null) return false;
        
        // Special handling for BigDecimal - use compareTo to ignore scale
        if (obj1 instanceof java.math.BigDecimal && obj2 instanceof java.math.BigDecimal) {
            return ((java.math.BigDecimal) obj1).compareTo((java.math.BigDecimal) obj2) == 0;
        }
        
        return obj1.equals(obj2);
    }
    
    /**
     * Get current authenticated user ID
     */
    private Integer getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                // Try to extract user ID from authentication details
                Object principal = authentication.getPrincipal();
                if (principal instanceof Integer) {
                    return (Integer) principal;
                }
                // Add more extraction logic based on your auth setup
                String username = authentication.getName();
                // You might need to fetch user from database by username
                log.debug("Authenticated user: {}", username);
                return 1; // Default to admin for now - UPDATE THIS based on your auth
            }
        } catch (Exception e) {
            log.error("Error getting current user ID", e);
        }
        return null;
    }
}
