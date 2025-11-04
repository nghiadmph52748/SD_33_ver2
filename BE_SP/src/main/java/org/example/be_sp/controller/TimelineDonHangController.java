package org.example.be_sp.controller;

import java.util.List;

import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.TimelineDonHangRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.model.response.TimelineDonHangResponse;
import org.example.be_sp.service.TimelineDonHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/timeline-don-hang")
@CrossOrigin(origins = "*")
@Slf4j
public class TimelineDonHangController {
    
    @Autowired
    private TimelineDonHangService timelineDonHangService;
    
    /**
     * Lấy tất cả timeline
     */
    @GetMapping
    public ResponseObject<List<TimelineDonHangResponse>> getAll() {
        return new ResponseObject<>(timelineDonHangService.getAll());
    }
    
    /**
     * Lấy timeline theo ID hoặc theo ID hóa đơn
     * Nếu tìm thấy timeline với ID đó -> trả về timeline đó
     * Nếu không tìm thấy -> coi như là ID hóa đơn và trả về danh sách timeline
     */
    @GetMapping("/{id}")
    public ResponseObject<?> getByIdOrHoaDonId(@PathVariable Integer id) {
        try {
            log.info("Lấy timeline với ID: {}", id);
            
            // Thử tìm timeline theo ID trước (thường timeline ID nhỏ hơn 1000)
            if (id != null && id > 0 && id < 1000) {
                try {
                    TimelineDonHangResponse timeline = timelineDonHangService.getById(id);
                    log.info("Tìm thấy timeline với ID: {}", id);
                    return new ResponseObject<>(timeline, "Lấy timeline thành công");
                } catch (ApiException e) {
                    log.warn("Không tìm thấy timeline với ID: {}, thử tìm theo ID hóa đơn", id);
                } catch (Exception e) {
                    log.error("Lỗi khi tìm timeline với ID: {}", id, e);
                }
            }
            
            // Coi như là ID hóa đơn và trả về danh sách timeline
            log.info("Tìm timeline theo ID hóa đơn: {}", id);
            List<TimelineDonHangResponse> timelines = timelineDonHangService.getByHoaDonId(id);
            log.info("Tìm thấy {} timeline cho hóa đơn ID: {}", timelines.size(), id);
            return new ResponseObject<>(timelines, "Lấy timeline theo hóa đơn thành công");
        } catch (Exception e) {
            log.error("Lỗi khi lấy timeline với ID: {}", id, e);
            e.printStackTrace();
            return ResponseObject.error("Lỗi khi lấy timeline: " + e.getMessage());
        }
    }
    
    /**
     * Tạo timeline mới
     */
    @PostMapping
    public ResponseObject<TimelineDonHangResponse> create(@RequestBody TimelineDonHangRequest request) {
        try {
            log.info("Tạo timeline mới với request: {}", request);
            TimelineDonHangResponse created = timelineDonHangService.create(request);
            log.info("Đã tạo timeline thành công với ID: {}", created.getId());
            return new ResponseObject<>(created, "Tạo timeline thành công");
        } catch (ApiException e) {
            log.error("Lỗi API khi tạo timeline: {}", e.getMessage(), e);
            return ResponseObject.error(e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi tạo timeline", e);
            e.printStackTrace();
            return ResponseObject.error("Lỗi khi tạo timeline: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật timeline
     */
    @PutMapping("/{id}")
    public ResponseObject<TimelineDonHangResponse> update(@PathVariable Integer id, @RequestBody TimelineDonHangRequest request) {
        TimelineDonHangResponse updated = timelineDonHangService.update(id, request);
        return new ResponseObject<>(updated, "Cập nhật timeline thành công");
    }
    
    /**
     * Xóa timeline
     */
    @DeleteMapping("/{id}")
    public ResponseObject<Boolean> delete(@PathVariable Integer id) {
        timelineDonHangService.delete(id);
        return new ResponseObject<>(true, null, "Xóa timeline thành công");
    }
}

