package org.example.be_sp.controller;

import org.example.be_sp.entity.LichLamViec;
import org.example.be_sp.entity.CaLamViec;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.model.request.LichLamViecRequest;
import org.example.be_sp.repository.LichLamViecRepository;
import org.example.be_sp.repository.CaLamViecRepository;
import org.example.be_sp.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/lich-lam-viec")
@CrossOrigin(origins = "*")
public class LichLamViecController {

    @Autowired
    private LichLamViecRepository lichLamViecRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private CaLamViecRepository caLamViecRepository;

    // 🟢 Lấy danh sách tất cả lịch làm việc
    @GetMapping
    public ResponseEntity<List<LichLamViec>> getAll() {
        return ResponseEntity.ok(lichLamViecRepository.findAll());
    }

    // 🟢 Lấy lịch làm việc theo id
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<LichLamViec> optional = lichLamViecRepository.findById(id);
        if (!optional.isPresent()) {
            return ResponseEntity.badRequest().body("Không tìm thấy lịch làm việc với id = " + id);
        }
        return ResponseEntity.ok(optional.get());
    }

    // 🟢 Thêm mới lịch làm việc
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> create(@RequestBody LichLamViecRequest request) {
        if (request.getNhanVienId() == null) {
            return ResponseEntity.badRequest().body("Thiếu thông tin nhân viên (id)");
        }
        if (request.getCaLamViecId() == null) {
            return ResponseEntity.badRequest().body("Thiếu thông tin ca làm việc (id)");
        }

        Optional<NhanVien> nhanVien = nhanVienRepository.findById(request.getNhanVienId());
        Optional<CaLamViec> caLamViec = caLamViecRepository.findById(request.getCaLamViecId());

        if (!nhanVien.isPresent()) {
            return ResponseEntity.badRequest().body("Không tìm thấy nhân viên có id = " + request.getNhanVienId());
        }
        if (!caLamViec.isPresent()) {
            return ResponseEntity.badRequest().body("Không tìm thấy ca làm việc có id = " + request.getCaLamViecId());
        }

        LichLamViec lichLamViec = new LichLamViec();
        lichLamViec.setNhanVien(nhanVien.get());
        lichLamViec.setCaLamViec(caLamViec.get());
        lichLamViec.setNgayLamViec(request.getNgayLamViec());
        lichLamViec.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        lichLamViec.setGhiChu(request.getGhiChu());

        LichLamViec saved = lichLamViecRepository.save(lichLamViec);
        return ResponseEntity.ok(saved);
    }


    // 🟡 Cập nhật lịch làm việc
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody LichLamViecRequest request) {
        Optional<LichLamViec> optional = lichLamViecRepository.findById(id);
        if (!optional.isPresent()) {
            return ResponseEntity.badRequest().body("Không tìm thấy lịch làm việc với id = " + id);
        }

        LichLamViec existing = optional.get();

        // Update NhanVien
        if (request.getNhanVienId() != null) {
            nhanVienRepository.findById(request.getNhanVienId()).ifPresent(existing::setNhanVien);
        }

        // Update CaLamViec
        if (request.getCaLamViecId() != null) {
            caLamViecRepository.findById(request.getCaLamViecId()).ifPresent(existing::setCaLamViec);
        }

        // Update các trường khác
        if (request.getNgayLamViec() != null) existing.setNgayLamViec(request.getNgayLamViec());
        if (request.getTrangThai() != null) existing.setTrangThai(request.getTrangThai());
        existing.setGhiChu(request.getGhiChu()); // có thể null, tùy ý

        LichLamViec updated = lichLamViecRepository.save(existing);
        return ResponseEntity.ok(updated);
    }


    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<?> toggleTrangThai(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Optional<LichLamViec> optional = lichLamViecRepository.findById(id);
        if (!optional.isPresent()) {
            return ResponseEntity.badRequest().body("Không tìm thấy lịch làm việc với id = " + id);
        }

        LichLamViec lich = optional.get();
        Boolean newStatus = body.get("trangThai");
        lich.setTrangThai(newStatus);

        lichLamViecRepository.save(lich);
        return ResponseEntity.ok(lich);
    }


}
