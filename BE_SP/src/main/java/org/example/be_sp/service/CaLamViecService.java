package org.example.be_sp.service;

import lombok.RequiredArgsConstructor;
import org.example.be_sp.entity.CaLamViec;
import org.example.be_sp.model.request.CaLamViecRequest;
import org.example.be_sp.repository.CaLamViecRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CaLamViecService {

    private final CaLamViecRepository caLamViecRepository;

    public List<CaLamViec> getAll() {
        return caLamViecRepository.findAll();
    }

    public CaLamViec create(CaLamViecRequest req) {
        CaLamViec ca = new CaLamViec();

        ca.setTenCa(req.getTenCa());
        ca.setThoiGianBatDau(req.getGioBatDau());
        ca.setThoiGianKetThuc(req.getGioKetThuc());
        ca.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : true); // mặc định hoạt động

        return caLamViecRepository.save(ca);
    }

    public CaLamViec update(Long id, CaLamViecRequest req) {
        CaLamViec ca = caLamViecRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc"));

        ca.setTenCa(req.getTenCa());

        // ✅ Kiểm tra null trước khi gán
        if (req.getGioBatDau() != null) {
            ca.setThoiGianBatDau(req.getGioBatDau());
        }

        if (req.getGioKetThuc() != null) {
            ca.setThoiGianKetThuc(req.getGioKetThuc());
        }



        return caLamViecRepository.save(ca);
    }


    public CaLamViec updateTrangThai(Long id, Boolean trangThai) {
        CaLamViec ca = caLamViecRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc id=" + id));
        ca.setTrangThai(trangThai);
        return caLamViecRepository.save(ca);
    }
    public Optional<CaLamViec> getById(Long id) {
        return caLamViecRepository.findById(id);
    }


}
