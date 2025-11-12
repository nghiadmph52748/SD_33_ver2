package org.example.be_sp.controller;

import lombok.RequiredArgsConstructor;
import org.example.be_sp.entity.GiaoCa;
import org.example.be_sp.model.request.GiaoCaRequest;
import org.example.be_sp.service.GiaoCaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/giao-ca")
@RequiredArgsConstructor
public class GiaoCaController {

    private final GiaoCaService giaoCaService;

    @GetMapping
    public List<GiaoCa> getAll() {
        return giaoCaService.getAll();
    }

    @GetMapping("/{id}")
    public GiaoCa getById(@PathVariable Long id) {
        return giaoCaService.getById(id);
    }

    @PostMapping
    public GiaoCa create(@RequestBody GiaoCaRequest req) {
        return giaoCaService.create(req);
    }

    @PutMapping("/{id}")
    public GiaoCa update(@PathVariable Long id, @RequestBody GiaoCaRequest req) {
        return giaoCaService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGiaoCa(@PathVariable Long id) {
        try {
            giaoCaService.delete(id);
            return ResponseEntity.ok("Xóa giao ca thành công!");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
