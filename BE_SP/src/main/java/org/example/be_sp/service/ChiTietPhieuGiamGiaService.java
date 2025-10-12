package org.example.be_sp.service;

import org.example.be_sp.entity.ChiTietPhieuGiamGia;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.ChiTietPhieuGiamGiaRequest;
import org.example.be_sp.model.response.ChiTietPhieuGiamGiaResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.ChiTietPhieuGiamGiaRepository;
import org.example.be_sp.repository.ChiTietSanPhamRepository;
import org.example.be_sp.repository.PhieuGiamGiaRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ChiTietPhieuGiamGiaService {
    @Autowired
    ChiTietPhieuGiamGiaRepository repository;
    @Autowired
    PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    ChiTietSanPhamRepository chiTietSanPhamRepository;

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
        System.out.println("[DEBUG] updateByPhieuGiamGia - idPhieuGiamGia: " + idPhieuGiamGia);
        System.out.println("[DEBUG] updateByPhieuGiamGia - idChiTietSanPhams: " + idChiTietSanPhams);
        System.out.println("[DEBUG] updateByPhieuGiamGia - size: " + (idChiTietSanPhams != null ? idChiTietSanPhams.size() : "NULL"));
        
        // Xóa tất cả chi tiết cũ
        repository.deleteByIdPhieuGiamGia(idPhieuGiamGia);
        System.out.println("[DEBUG] Deleted old product details for coupon: " + idPhieuGiamGia);
        
        // Thêm mới
        if (idChiTietSanPhams != null && !idChiTietSanPhams.isEmpty()) {
            addMultiple(idPhieuGiamGia, idChiTietSanPhams);
            System.out.println("[DEBUG] Added " + idChiTietSanPhams.size() + " product details");
        } else {
            System.out.println("[DEBUG] No products to add (empty list)");
        }
    }

    public void updateStatus(Integer id) {
        ChiTietPhieuGiamGia c = repository.findById(id)
                .orElseThrow(() -> new ApiException("Chi tiết phiếu giảm giá không tồn tại", "404"));
        c.setDeleted(true);
        repository.save(c);
    }
}

