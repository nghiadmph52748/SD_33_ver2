package org.example.be_sp.service;

import org.example.be_sp.entity.PhuongThucThanhToan;
import org.example.be_sp.model.request.PhuongThucThanhToanRequest;
import org.example.be_sp.model.response.PhuongThucThanhToanResponse;
import org.example.be_sp.repository.PhuongThucThanhToanRepository;
import org.example.be_sp.util.GenericCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class PhuongThucThanhToanService extends GenericCrudService<PhuongThucThanhToan, Integer, PhuongThucThanhToanResponse, PhuongThucThanhToanRequest> {
    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    public PhuongThucThanhToanService(Class<PhuongThucThanhToan> entity, Class<PhuongThucThanhToanResponse> phuongThucThanhToanResponseClass, Class<PhuongThucThanhToanRequest> phuongThucThanhToanRequestClass, JpaRepository<PhuongThucThanhToan, Integer> repository) {
        super(entity, phuongThucThanhToanResponseClass, phuongThucThanhToanRequestClass, repository);
    }
    public void updateStatus(Integer id) {
        PhuongThucThanhToan phuongThucThanhToan = phuongThucThanhToanRepository.findById(id).orElseThrow(() -> new RuntimeException("Phương thức thanh toán không tồn tại"));
        phuongThucThanhToan.setDeleted(true);
        phuongThucThanhToanRepository.save(phuongThucThanhToan);
    }

}
