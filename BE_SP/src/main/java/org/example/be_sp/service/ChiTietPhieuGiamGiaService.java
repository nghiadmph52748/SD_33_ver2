package org.example.be_sp.service;

import org.example.be_sp.entity.ChiTietPhieuGiamGia;
import org.example.be_sp.entity.PhieuGiamGiaHistory;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.ChiTietPhieuGiamGiaRequest;
import org.example.be_sp.model.response.ChiTietPhieuGiamGiaResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.ChiTietPhieuGiamGiaRepository;
import org.example.be_sp.repository.ChiTietSanPhamRepository;
import org.example.be_sp.repository.PhieuGiamGiaRepository;
import org.example.be_sp.repository.PhieuGiamGiaHistoryRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ChiTietPhieuGiamGiaService {
    @Autowired
    ChiTietPhieuGiamGiaRepository repository;
    @Autowired
    PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired
    PhieuGiamGiaHistoryRepository historyRepository;

    public List<ChiTietPhieuGiamGiaResponse> getAll() {
        return repository.findAll().stream().map(ChiTietPhieuGiamGiaResponse::new).toList();
    }

    public PagingResponse<ChiTietPhieuGiamGiaResponse> paging(int page, int size) {
        return new PagingResponse<>(repository.findAll(PageRequest.of(page, size)).map(ChiTietPhieuGiamGiaResponse::new), page);
    }

    public ChiTietPhieuGiamGiaResponse getById(Integer id) {
        return repository.findById(id).map(ChiTietPhieuGiamGiaResponse::new)
                .orElseThrow(() -> new ApiException("Chi tiết phiếu giảm giá không tồn tại", "404"));
    }

    public List<ChiTietPhieuGiamGiaResponse> getByIdPhieuGiamGia(Integer idPhieuGiamGia) {
        return repository.findByIdPhieuGiamGia(idPhieuGiamGia).stream()
                .map(ChiTietPhieuGiamGiaResponse::new).toList();
    }

    public void add(ChiTietPhieuGiamGiaRequest request) {
        ChiTietPhieuGiamGia e = MapperUtils.map(request, ChiTietPhieuGiamGia.class);
        e.setIdChiTietSanPham(chiTietSanPhamRepository.findChiTietSanPhamById(request.getIdChiTietSanPham()));
        e.setIdPhieuGiamGia(phieuGiamGiaRepository.findPhieuGiamGiaById(request.getIdPhieuGiamGia()));
        repository.save(e);
    }

    @Transactional
    public void addMultiple(Integer idPhieuGiamGia, List<Integer> idChiTietSanPhams) {
        var phieuGiamGia = phieuGiamGiaRepository.findPhieuGiamGiaById(idPhieuGiamGia);
        if (phieuGiamGia == null) {
            throw new ApiException("Phiếu giảm giá không tồn tại", "404");
        }

        for (Integer idChiTietSanPham : idChiTietSanPhams) {
            var chiTietSanPham = chiTietSanPhamRepository.findChiTietSanPhamById(idChiTietSanPham);
            if (chiTietSanPham != null) {
                ChiTietPhieuGiamGia e = new ChiTietPhieuGiamGia();
                e.setIdPhieuGiamGia(phieuGiamGia);
                e.setIdChiTietSanPham(chiTietSanPham);
                e.setTrangThai(true);
                e.setDeleted(false);
                e.setCreateAt(LocalDate.now());
                repository.save(e);
            }
        }
    }

    public void update(Integer id, ChiTietPhieuGiamGiaRequest request) {
        ChiTietPhieuGiamGia ex = repository.findById(id)
                .orElseThrow(() -> new ApiException("Chi tiết phiếu giảm giá không tồn tại " + id, "404"));
        ChiTietPhieuGiamGia e = MapperUtils.map(request, ChiTietPhieuGiamGia.class);
        e.setId(id);
        e.setIdChiTietSanPham(chiTietSanPhamRepository.findChiTietSanPhamById(request.getIdChiTietSanPham()));
        e.setIdPhieuGiamGia(phieuGiamGiaRepository.findPhieuGiamGiaById(request.getIdPhieuGiamGia()));
        e.setDeleted(ex.getDeleted());
        e.setTrangThai(ex.getTrangThai());
        e.setCreateAt(ex.getCreateAt());
        e.setCreateBy(ex.getCreateBy());
        repository.save(e);
    }

    @Transactional
    public void updateByPhieuGiamGia(Integer idPhieuGiamGia, List<Integer> idChiTietSanPhams) {
        log.debug("[PRODUCT_UPDATE] Updating products for coupon {}", idPhieuGiamGia);
        
        // Get old product IDs before deletion
        List<Integer> oldProductIds = repository.findByIdPhieuGiamGia(idPhieuGiamGia).stream()
                .map(ct -> ct.getIdChiTietSanPham().getId())
                .sorted()
                .toList();
        
        // Prepare new product IDs
        List<Integer> newProductIds = idChiTietSanPhams != null ? 
                idChiTietSanPhams.stream().sorted().toList() : List.of();
        
        // Xóa tất cả chi tiết cũ
        repository.deleteByIdPhieuGiamGia(idPhieuGiamGia);
        log.debug("[PRODUCT_UPDATE] Deleted {} old products", oldProductIds.size());
        
        // Thêm mới
        if (!newProductIds.isEmpty()) {
            addMultiple(idPhieuGiamGia, newProductIds);
            log.debug("[PRODUCT_UPDATE] Added {} new products", newProductIds.size());
        }
        
        // Log product changes to history
        String changes = buildProductChanges(oldProductIds, newProductIds);
        if (!changes.isEmpty()) {
            logHistory(idPhieuGiamGia, "CẬP NHẬT", changes);
        }
    }
    
    /**
     * Build product changes description
     */
    private String buildProductChanges(List<Integer> oldProductIds, List<Integer> newProductIds) {
        if (oldProductIds.equals(newProductIds)) {
            return "";
        }
        
        StringBuilder changes = new StringBuilder();
        
        // Find added products
        List<Integer> added = newProductIds.stream()
                .filter(id -> !oldProductIds.contains(id))
                .toList();
        
        // Find removed products
        List<Integer> removed = oldProductIds.stream()
                .filter(id -> !newProductIds.contains(id))
                .toList();
        
        if (!added.isEmpty() || !removed.isEmpty()) {
            changes.append("Sản phẩm áp dụng: ");
            
            if (oldProductIds.isEmpty() && !newProductIds.isEmpty()) {
                changes.append(String.format("Thêm %d sản phẩm", newProductIds.size()));
            } else if (!oldProductIds.isEmpty() && newProductIds.isEmpty()) {
                changes.append(String.format("Xóa tất cả %d sản phẩm", oldProductIds.size()));
            } else {
                List<String> changeParts = new java.util.ArrayList<>();
                if (!added.isEmpty()) {
                    changeParts.add(String.format("Thêm %d", added.size()));
                }
                if (!removed.isEmpty()) {
                    changeParts.add(String.format("Xóa %d", removed.size()));
                }
                changes.append(String.join(", ", changeParts));
                changes.append(String.format(" (%d → %d sản phẩm)", oldProductIds.size(), newProductIds.size()));
            }
        }
        
        return changes.toString();
    }
    
    /**
     * Log a history entry for product changes
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
            log.info("Logged product change history for coupon {}", idPhieuGiamGia);
        } catch (Exception e) {
            log.error("Failed to log product change history for coupon {}", idPhieuGiamGia, e);
        }
    }
    
    /**
     * Get current authenticated user ID
     */
    private Integer getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                return 1; // Default to admin - UPDATE based on your auth setup
            }
        } catch (Exception e) {
            log.error("Error getting current user ID", e);
        }
        return null;
    }

    public void updateStatus(Integer id) {
        ChiTietPhieuGiamGia c = repository.findById(id)
                .orElseThrow(() -> new ApiException("Chi tiết phiếu giảm giá không tồn tại", "404"));
        c.setDeleted(true);
        repository.save(c);
    }
}

