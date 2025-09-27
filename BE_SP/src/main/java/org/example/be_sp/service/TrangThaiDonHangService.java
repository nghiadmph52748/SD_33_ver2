package org.example.be_sp.service;

import org.example.be_sp.entity.TrangThaiDonHang;
import org.example.be_sp.model.request.TrangThaiDonHangRequest;
import org.example.be_sp.model.response.TrangThaiDonHangResponse;
import org.example.be_sp.repository.TrangThaiDonHangRepository;
import org.example.be_sp.util.GenericCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class TrangThaiDonHangService extends GenericCrudService<TrangThaiDonHang,Integer, TrangThaiDonHangResponse, TrangThaiDonHangRequest> {

    @Autowired
    private TrangThaiDonHangRepository trangThaiDonHangRepository;

    public TrangThaiDonHangService(Class<TrangThaiDonHang> entity, Class<TrangThaiDonHangResponse> trangThaiDonHangResponseClass, Class<TrangThaiDonHangRequest> trangThaiDonHangRequestClass, JpaRepository<TrangThaiDonHang,Integer> repository) {
        super(entity, trangThaiDonHangResponseClass, trangThaiDonHangRequestClass, repository);
    }

    public void updateStatus(Integer id) {
        TrangThaiDonHang trangThaiDonHang = trangThaiDonHangRepository.findById(id).orElseThrow(() -> new RuntimeException("Trạng thái đơn hàng không tồn tại"));
        trangThaiDonHang.setDeleted(true);
        trangThaiDonHangRepository.save(trangThaiDonHang);
    }
}
