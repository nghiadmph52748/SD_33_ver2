package org.example.be_sp.repository;

import org.example.be_sp.entity.HinhThucThanhToan;
import org.example.be_sp.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HinhThucThanhToanRepository extends JpaRepository<HinhThucThanhToan, Integer> {
    HinhThucThanhToan findByIdHoaDonAndTrangThaiAndDeleted(HoaDon idHoaDon, Boolean trangThai, Boolean deleted);
    List<HinhThucThanhToan> findByIdHoaDonAndDeleted(HoaDon idHoaDon, Boolean deleted);
}
