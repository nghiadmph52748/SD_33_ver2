package org.example.be_sp.controller;

import lombok.RequiredArgsConstructor;
import org.example.be_sp.entity.CaLamViec;
import org.example.be_sp.model.request.CaLamViecRequest;
import org.example.be_sp.service.CaLamViecService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ca-lam-viec")
@RequiredArgsConstructor
public class CaLamViecController {

    private final CaLamViecService caLamViecService;

    @GetMapping
    public List<CaLamViec> getAll() {
        return caLamViecService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaLamViec> getById(@PathVariable Long id) {
        return caLamViecService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CaLamViec> create(@RequestBody CaLamViecRequest req) {
        CaLamViec created = caLamViecService.create(req);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CaLamViec> update(@PathVariable Long id, @RequestBody CaLamViecRequest req) {
        CaLamViec updated = caLamViecService.update(id, req);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<?> toggleTrangThai(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean newStatus = body.get("trangThai"); // trạng thái mới từ frontend
        if (newStatus == null) {
            return ResponseEntity.badRequest().body("Thiếu trường 'trangThai'");
        }

        try {
            CaLamViec updated = caLamViecService.updateTrangThai(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
