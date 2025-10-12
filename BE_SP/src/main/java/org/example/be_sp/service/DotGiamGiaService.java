package org.example.be_sp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.example.be_sp.entity.DotGiamGia;
import org.example.be_sp.entity.DotGiamGiaHistory;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.DotGiamGiaRequest;
import org.example.be_sp.model.response.DotGiamGiaHistoryResponse;
import org.example.be_sp.model.response.DotGiamGiaResponse;
import org.example.be_sp.repository.DotGiamGiaHistoryRepository;
import org.example.be_sp.repository.DotGiamGiaRepository;
import org.example.be_sp.repository.NhanVienRepository;
import org.example.be_sp.util.GenericCrudService;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DotGiamGiaService extends GenericCrudService<DotGiamGia, Integer, DotGiamGiaResponse, DotGiamGiaRequest> {
    public DotGiamGiaService(Class<DotGiamGia> entity, Class<DotGiamGiaResponse> dotGiamGiaResponseClass,
            Class<DotGiamGiaRequest> dotGiamGiaRequestClass, JpaRepository<DotGiamGia, Integer> repository) {
        super(entity, dotGiamGiaResponseClass, dotGiamGiaRequestClass, repository);
    }

    @Autowired
    DotGiamGiaRepository repository;
    
    @Autowired
    DotGiamGiaHistoryRepository historyRepository;
    
    @Autowired
    NhanVienRepository nhanVienRepository;
    
    /**
     * Override add to include timestamp and history logging
     */
    @Override
    public DotGiamGia add(DotGiamGiaRequest request) {
        DotGiamGia dgg = MapperUtils.map(request, DotGiamGia.class);
        dgg.setCreatedAt(LocalDateTime.now());
        dgg.setUpdatedAt(LocalDateTime.now());
        DotGiamGia saved = repository.save(dgg);
        
        // Log creation
        logHistory(saved.getId(), "TẠO MỚI", "Tạo đợt giảm giá: " + saved.getTenDotGiamGia());
        
        return saved;
    }
    
    /**
     * Override update to include timestamp, change tracking, and history logging
     */
    @Override
    public DotGiamGia update(Integer id, DotGiamGiaRequest request) {
        // Fetch existing entity
        DotGiamGia existingDgg = repository.findById(id)
                .orElseThrow(() -> new ApiException("DotGiamGia not found", "404"));
        
        // Create snapshot of old values
        DotGiamGia oldSnapshot = new DotGiamGia();
        oldSnapshot.setTenDotGiamGia(existingDgg.getTenDotGiamGia());
        oldSnapshot.setGiaTriGiamGia(existingDgg.getGiaTriGiamGia());
        oldSnapshot.setNgayBatDau(existingDgg.getNgayBatDau());
        oldSnapshot.setNgayKetThuc(existingDgg.getNgayKetThuc());
        oldSnapshot.setTrangThai(existingDgg.getTrangThai());
        
        // Map request to updated entity
        DotGiamGia updatedDgg = MapperUtils.map(request, DotGiamGia.class);
        updatedDgg.setId(id);
        
        // Preserve generated field
        updatedDgg.setMaDotGiamGia(existingDgg.getMaDotGiamGia());
        
        // Preserve createdAt and update updatedAt
        updatedDgg.setCreatedAt(existingDgg.getCreatedAt());
        updatedDgg.setUpdatedAt(LocalDateTime.now());
        
        DotGiamGia saved = repository.save(updatedDgg);
        
        // Build change description and log history
        String changes = buildChangeDescription(oldSnapshot, updatedDgg);
        logHistory(id, "CẬP NHẬT", changes);
        
        return saved;
    }

    public void updateStatus(Integer id) {
        DotGiamGia e = repository.findById(id).orElseThrow(() -> new ApiException("DotGiamGia not found", "404"));
        boolean oldStatus = Boolean.TRUE.equals(e.getDeleted());
        e.setDeleted(true);
        e.setUpdatedAt(LocalDateTime.now());
        repository.save(e);
        
        // Log status change
        if (!oldStatus) {
            logHistory(id, "CẬP NHẬT", "Thay đổi trạng thái xóa: Không → Có");
        }
    }

    public void delete(Integer id) {
        // Verify the entity exists before deleting
        if (!repository.existsById(id)) {
            throw new ApiException("DotGiamGia not found", "404");
        }
        
        DotGiamGia dgg = repository.getById(id);
        
        // Log deletion before actually deleting
        logHistory(id, "XÓA", "Xóa đợt giảm giá: " + dgg.getTenDotGiamGia());
        
        // Hard delete from database
        repository.deleteById(id);
    }
    
    /**
     * Get change history for a promotion campaign
     */
    public List<DotGiamGiaHistoryResponse> getHistory(Integer idDotGiamGia) {
        List<DotGiamGiaHistory> history = historyRepository.findByIdDotGiamGiaOrderByNgayThayDoiDesc(idDotGiamGia);
        return history.stream()
                .map(h -> DotGiamGiaHistoryResponse.fromEntity(h, nhanVienRepository))
                .toList();
    }
    
    /**
     * Log a history entry for a promotion campaign change
     */
    private void logHistory(Integer idDotGiamGia, String action, String description) {
        try {
            Integer userId = getCurrentUserId();
            if (userId == null) {
                log.warn("Cannot log history: No authenticated user found");
                return;
            }
            
            DotGiamGiaHistory history = DotGiamGiaHistory.builder()
                    .idDotGiamGia(idDotGiamGia)
                    .idNhanVien(userId)
                    .hanhDong(action)
                    .moTaThayDoi(description)
                    .ngayThayDoi(LocalDateTime.now())
                    .build();
            
            historyRepository.save(history);
            log.info("Logged history for promotion campaign {} - Action: {}", idDotGiamGia, action);
        } catch (Exception e) {
            log.error("Failed to log history for promotion campaign {}", idDotGiamGia, e);
            // Don't throw - history logging should not break main flow
        }
    }
    
    /**
     * Build a description of changes between old and new promotion campaign
     */
    private String buildChangeDescription(DotGiamGia oldDgg, DotGiamGia newDgg) {
        StringBuilder changes = new StringBuilder();
        
        // Tên đợt giảm giá
        if (!safeEquals(oldDgg.getTenDotGiamGia(), newDgg.getTenDotGiamGia())) {
            changes.append(String.format("Tên: '%s' → '%s'\n", 
                    oldDgg.getTenDotGiamGia(), newDgg.getTenDotGiamGia()));
        }
        
        // Giá trị giảm
        if (!safeEquals(oldDgg.getGiaTriGiamGia(), newDgg.getGiaTriGiamGia())) {
            changes.append(String.format("Giá trị giảm: %s%% → %s%%\n", 
                    formatNumber(oldDgg.getGiaTriGiamGia()), formatNumber(newDgg.getGiaTriGiamGia())));
        }
        
        // Ngày bắt đầu
        if (!safeEquals(oldDgg.getNgayBatDau(), newDgg.getNgayBatDau())) {
            changes.append(String.format("Ngày bắt đầu: %s → %s\n", 
                    formatDateTime(oldDgg.getNgayBatDau()), formatDateTime(newDgg.getNgayBatDau())));
        }
        
        // Ngày kết thúc
        if (!safeEquals(oldDgg.getNgayKetThuc(), newDgg.getNgayKetThuc())) {
            changes.append(String.format("Ngày kết thúc: %s → %s\n", 
                    formatDateTime(oldDgg.getNgayKetThuc()), formatDateTime(newDgg.getNgayKetThuc())));
        }
        
        // Trạng thái
        if (!safeEquals(oldDgg.getTrangThai(), newDgg.getTrangThai())) {
            String oldStatus = Boolean.TRUE.equals(oldDgg.getTrangThai()) ? "Hoạt động" : "Tạm dừng";
            String newStatus = Boolean.TRUE.equals(newDgg.getTrangThai()) ? "Hoạt động" : "Tạm dừng";
            changes.append(String.format("Trạng thái: %s → %s\n", oldStatus, newStatus));
        }
        
        String result = changes.length() > 0 ? changes.toString().trim() : "Không có thay đổi";
        return result;
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
     */
    private boolean safeEquals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;
        if (obj1 == null || obj2 == null) return false;
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
                Object principal = authentication.getPrincipal();
                
                // If principal is already an Integer (shouldn't happen but check anyway)
                if (principal instanceof Integer) {
                    return (Integer) principal;
                }
                
                // Get username and lookup employee
                String username = authentication.getName();
                if (username != null && !username.isEmpty()) {
                    try {
                        return nhanVienRepository.findByTenTaiKhoan(username)
                                .map(org.example.be_sp.entity.NhanVien::getId)
                                .orElseGet(() -> {
                                    log.warn("No employee found for username: {}", username);
                                    return null;
                                });
                    } catch (Exception e) {
                        log.error("Error looking up employee by username: {}", username, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting current user ID", e);
        }
        return null;
    }
}
