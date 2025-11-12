package org.example.be_sp.service;

import lombok.RequiredArgsConstructor;
import org.example.be_sp.entity.CaLamViec;
import org.example.be_sp.entity.LichLamViec;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.model.request.LichLamViecRequest;
import org.example.be_sp.repository.CaLamViecRepository;
import org.example.be_sp.repository.LichLamViecRepository;
import org.example.be_sp.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class LichLamViecService {

    @Autowired
    private LichLamViecRepository repository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private CaLamViecRepository caLamViecRepository;

    public List<LichLamViec> getAll() {
        return repository.findAll();
    }

    public LichLamViec create(LichLamViec lichLamViec) {
        // 🔹 Lấy entity thật từ DB
        var nhanVien = nhanVienRepository.findById(lichLamViec.getNhanVien().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));
        var caLamViec = caLamViecRepository.findById(lichLamViec.getCaLamViec().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc!"));

        // 🔹 Gán lại entity đã load vào đối tượng chính
        lichLamViec.setNhanVien(nhanVien);
        lichLamViec.setCaLamViec(caLamViec);

        return repository.save(lichLamViec);
    }

    public LichLamViec update(Long id, LichLamViec lichLamViec) {
        LichLamViec existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch làm việc!"));

        var nhanVien = nhanVienRepository.findById(lichLamViec.getNhanVien().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));
        var caLamViec = caLamViecRepository.findById(lichLamViec.getCaLamViec().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc!"));

        existing.setNhanVien(nhanVien);
        existing.setCaLamViec(caLamViec);
        existing.setNgayLamViec(lichLamViec.getNgayLamViec());
        existing.setTrangThai(lichLamViec.getTrangThai());
        existing.setGhiChu(lichLamViec.getGhiChu());

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
