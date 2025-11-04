package org.example.be_sp.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.entity.TimelineDonHang;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.TimelineDonHangRequest;
import org.example.be_sp.model.response.TimelineDonHangResponse;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.repository.NhanVienRepository;
import org.example.be_sp.repository.TimelineDonHangRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TimelineDonHangService {
    
    @Autowired
    private TimelineDonHangRepository timelineDonHangRepository;
    
    @Autowired
    private HoaDonRepository hoaDonRepository;
    
    @Autowired
    private NhanVienRepository nhanVienRepository;
    
    /**
     * Lấy tất cả timeline
     */
    public List<TimelineDonHangResponse> getAll() {
        return timelineDonHangRepository.findAll().stream()
            .map(TimelineDonHangResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy timeline theo ID
     */
    @Transactional(readOnly = true)
    public TimelineDonHangResponse getById(Integer id) {
        TimelineDonHang timeline = timelineDonHangRepository.findById(id)
            .orElseThrow(() -> new ApiException("Không tìm thấy timeline", "404"));
        
        // Force load các lazy entities
        if (timeline.getIdHoaDon() != null) {
            Hibernate.initialize(timeline.getIdHoaDon());
        }
        if (timeline.getIdNhanVien() != null) {
            Hibernate.initialize(timeline.getIdNhanVien());
        }
        
        return new TimelineDonHangResponse(timeline);
    }
    
    /**
     * Lấy timeline theo ID hóa đơn
     */
    @Transactional(readOnly = true)
    public List<TimelineDonHangResponse> getByHoaDonId(Integer hoaDonId) {
        try {
            List<TimelineDonHang> timelines = timelineDonHangRepository
                .findByHoaDonId(hoaDonId);
            
            if (timelines == null || timelines.isEmpty()) {
                log.warn("Không tìm thấy timeline cho hóa đơn ID: {}", hoaDonId);
                return List.of();
            }
            
            // Force load các lazy entities trước khi tạo response
            return timelines.stream()
                .map(timeline -> {
                    // Force load entities để tránh LazyInitializationException
                    if (timeline.getIdHoaDon() != null) {
                        Hibernate.initialize(timeline.getIdHoaDon());
                    }
                    if (timeline.getIdNhanVien() != null) {
                        Hibernate.initialize(timeline.getIdNhanVien());
                    }
                    return new TimelineDonHangResponse(timeline);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi lấy timeline cho hóa đơn ID: {}", hoaDonId, e);
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Tạo timeline mới
     */
    @Transactional
    public TimelineDonHangResponse create(TimelineDonHangRequest request) {
        try {
            log.info("Bắt đầu tạo timeline với request: idHoaDon={}, trangThaiMoi={}, hanhDong={}", 
                request.getIdHoaDon(), request.getTrangThaiMoi(), request.getHanhDong());
            
            // Validate required fields
            if (request == null) {
                throw new ApiException("Request không được để trống", "400");
            }
            if (request.getIdHoaDon() == null) {
                throw new ApiException("ID hóa đơn không được để trống", "400");
            }
            if (request.getTrangThaiMoi() == null || request.getTrangThaiMoi().trim().isEmpty()) {
                throw new ApiException("Trạng thái mới không được để trống", "400");
            }
            if (request.getHanhDong() == null || request.getHanhDong().trim().isEmpty()) {
                throw new ApiException("Hành động không được để trống", "400");
            }
            
            // Tìm hóa đơn
            log.debug("Tìm hóa đơn với ID: {}", request.getIdHoaDon());
            HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> {
                    log.error("Không tìm thấy hóa đơn với ID: {}", request.getIdHoaDon());
                    return new ApiException("Hóa đơn không tồn tại", "404");
                });
            log.debug("Đã tìm thấy hóa đơn: {}", hoaDon.getId());
            
            // Tìm nhân viên (ưu tiên từ request, sau đó từ hóa đơn)
            NhanVien nhanVien = null;
            if (request.getIdNhanVien() != null && request.getIdNhanVien() > 0) {
                log.debug("Tìm nhân viên với ID: {}", request.getIdNhanVien());
                nhanVien = nhanVienRepository.findById(request.getIdNhanVien())
                    .orElse(null);
                if (nhanVien == null) {
                    log.warn("Không tìm thấy nhân viên với ID: {}, sử dụng nhân viên từ hóa đơn", request.getIdNhanVien());
                    nhanVien = hoaDon.getIdNhanVien();
                } else {
                    log.debug("Đã tìm thấy nhân viên: {}", nhanVien.getTenNhanVien());
                }
            } else {
                // Sử dụng nhân viên từ hóa đơn nếu request không có
                log.debug("Sử dụng nhân viên từ hóa đơn");
                nhanVien = hoaDon.getIdNhanVien();
            }
            
            // Validate nhân viên
            if (nhanVien == null) {
                log.error("Không tìm thấy nhân viên cho timeline");
                throw new ApiException("Không tìm thấy nhân viên", "400");
            }
            
            // Tạo entity mới
            log.debug("Tạo TimelineDonHang entity");
            TimelineDonHang timeline = new TimelineDonHang();
            timeline.setIdHoaDon(hoaDon);
            timeline.setIdNhanVien(nhanVien);
            timeline.setTrangThaiCu(request.getTrangThaiCu());
            timeline.setTrangThaiMoi(request.getTrangThaiMoi());
            timeline.setHanhDong(request.getHanhDong());
            timeline.setMoTa(request.getMoTa() != null ? request.getMoTa() : "");
            timeline.setGhiChu(request.getGhiChu() != null ? request.getGhiChu() : "");
            timeline.setIpAddress(request.getIpAddress());
            timeline.setUserAgent(request.getUserAgent());
            
            // Parse thời gian
            if (request.getThoiGian() != null && !request.getThoiGian().trim().isEmpty()) {
                try {
                    // Format: "YYYY-MM-DD HH:mm:ss"
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime localDateTime = LocalDateTime.parse(request.getThoiGian(), formatter);
                    timeline.setThoiGian(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                    log.debug("Đã parse thời gian: {}", timeline.getThoiGian());
                } catch (Exception e) {
                    log.warn("Không thể parse thời gian: {}, sử dụng thời gian hiện tại", request.getThoiGian(), e);
                    timeline.setThoiGian(Instant.now());
                }
            } else {
                timeline.setThoiGian(Instant.now());
                log.debug("Sử dụng thời gian hiện tại: {}", timeline.getThoiGian());
            }
            
            // Set default values
            timeline.setTrangThai(true);
            timeline.setDeleted(false);
            
            // Lưu vào database
            log.debug("Lưu timeline vào database");
            TimelineDonHang saved = timelineDonHangRepository.save(timeline);
            log.info("Đã tạo timeline mới với ID: {} cho hóa đơn ID: {}", saved.getId(), hoaDon.getId());
            
            // Force load entities trước khi tạo response
            if (saved.getIdHoaDon() != null) {
                Hibernate.initialize(saved.getIdHoaDon());
            }
            if (saved.getIdNhanVien() != null) {
                Hibernate.initialize(saved.getIdNhanVien());
            }
            
            log.debug("Tạo TimelineDonHangResponse");
            TimelineDonHangResponse response = new TimelineDonHangResponse(saved);
            log.info("Tạo timeline thành công với ID: {}", response.getId());
            return response;
            
        } catch (ApiException e) {
            log.error("Lỗi API khi tạo timeline: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Lỗi không mong đợi khi tạo timeline", e);
            e.printStackTrace();
            throw new ApiException("Lỗi khi tạo timeline: " + e.getMessage(), "500");
        }
    }
    
    /**
     * Cập nhật timeline
     */
    @Transactional
    public TimelineDonHangResponse update(Integer id, TimelineDonHangRequest request) {
        TimelineDonHang timeline = timelineDonHangRepository.findById(id)
            .orElseThrow(() -> new ApiException("Không tìm thấy timeline", "404"));
        
        // Cập nhật các trường
        if (request.getTrangThaiMoi() != null) {
            timeline.setTrangThaiMoi(request.getTrangThaiMoi());
        }
        if (request.getHanhDong() != null) {
            timeline.setHanhDong(request.getHanhDong());
        }
        if (request.getMoTa() != null) {
            timeline.setMoTa(request.getMoTa());
        }
        if (request.getGhiChu() != null) {
            timeline.setGhiChu(request.getGhiChu());
        }
        if (request.getThoiGian() != null && !request.getThoiGian().trim().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime localDateTime = LocalDateTime.parse(request.getThoiGian(), formatter);
                timeline.setThoiGian(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
            } catch (Exception e) {
                log.warn("Không thể parse thời gian: {}", request.getThoiGian(), e);
            }
        }
        
        TimelineDonHang updated = timelineDonHangRepository.save(timeline);
        
        // Force load entities trước khi tạo response
        if (updated.getIdHoaDon() != null) {
            Hibernate.initialize(updated.getIdHoaDon());
        }
        if (updated.getIdNhanVien() != null) {
            Hibernate.initialize(updated.getIdNhanVien());
        }
        
        return new TimelineDonHangResponse(updated);
    }
    
    /**
     * Xóa timeline (soft delete)
     */
    @Transactional
    public void delete(Integer id) {
        TimelineDonHang timeline = timelineDonHangRepository.findById(id)
            .orElseThrow(() -> new ApiException("Không tìm thấy timeline", "404"));
        
        timeline.setDeleted(true);
        timelineDonHangRepository.save(timeline);
        log.info("Đã xóa timeline với ID: {}", id);
    }
}

