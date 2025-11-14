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

import java.time.LocalDate;
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
        // Validate cơ bản
        if (request.getNhanVienId() == null) return ResponseEntity.badRequest().body("Thiếu thông tin nhân viên (id)");
        if (request.getCaLamViecId() == null) return ResponseEntity.badRequest().body("Thiếu thông tin ca làm việc (id)");
        if (request.getNgayLamViec() == null) return ResponseEntity.badRequest().body("Thiếu thông tin ngày làm việc");

        Optional<NhanVien> nhanVien = nhanVienRepository.findById(request.getNhanVienId());
        Optional<CaLamViec> caLamViec = caLamViecRepository.findById(request.getCaLamViecId());
        if (!nhanVien.isPresent()) return ResponseEntity.badRequest().body("Không tìm thấy nhân viên");
        if (!caLamViec.isPresent()) return ResponseEntity.badRequest().body("Không tìm thấy ca làm việc");

        // 🔹 Check trùng lịch
        if (isTrungLich(nhanVien.get(), caLamViec.get(), request.getNgayLamViec(), null)) {
            return ResponseEntity.badRequest().body("Nhân viên đã có lịch làm việc vào ca này!");
        }

        LichLamViec lichLamViec = new LichLamViec();
        lichLamViec.setNhanVien(nhanVien.get());
        lichLamViec.setCaLamViec(caLamViec.get());
        lichLamViec.setNgayLamViec(request.getNgayLamViec());
        lichLamViec.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        lichLamViec.setGhiChu(request.getGhiChu());

        return ResponseEntity.ok(lichLamViecRepository.save(lichLamViec));
    }



    // 🟡 Cập nhật lịch làm việc
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody LichLamViecRequest request) {
        Optional<LichLamViec> optional = lichLamViecRepository.findById(id);
        if (!optional.isPresent()) return ResponseEntity.badRequest().body("Không tìm thấy lịch làm việc với id = " + id);

        LichLamViec existing = optional.get();

        // Xác định NhanVien và CaLamViec mới
        NhanVien nhanVien = request.getNhanVienId() != null
                ? nhanVienRepository.findById(request.getNhanVienId()).orElse(existing.getNhanVien())
                : existing.getNhanVien();

        CaLamViec caLamViec = request.getCaLamViecId() != null
                ? caLamViecRepository.findById(request.getCaLamViecId()).orElse(existing.getCaLamViec())
                : existing.getCaLamViec();

        LocalDate ngayLamViec = request.getNgayLamViec() != null
                ? request.getNgayLamViec()
                : existing.getNgayLamViec();

        // 🔹 Check trùng lịch, loại trừ chính lịch hiện tại
        if (isTrungLich(nhanVien, caLamViec, ngayLamViec, existing.getId())) {
            return ResponseEntity.badRequest().body("Nhân viên đã có lịch làm việc vào ca này!");
        }

        // Update entity
        existing.setNhanVien(nhanVien);
        existing.setCaLamViec(caLamViec);
        existing.setNgayLamViec(ngayLamViec);
        if (request.getTrangThai() != null) existing.setTrangThai(request.getTrangThai());
        existing.setGhiChu(request.getGhiChu());

        return ResponseEntity.ok(lichLamViecRepository.save(existing));
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
    private boolean isTrungLich(NhanVien nhanVien, CaLamViec caLamViec, LocalDate ngayLamViec, Long excludeId) {
        Long idCheck = (excludeId == null) ? -1L : excludeId;
        return lichLamViecRepository.existsByNhanVienAndCaLamViecAndNgayLamViecAndIdNot(
                nhanVien, caLamViec, ngayLamViec, idCheck
        );
    }



}
