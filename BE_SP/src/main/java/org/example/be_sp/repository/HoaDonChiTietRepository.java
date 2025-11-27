package org.example.be_sp.repository;

import java.util.ArrayList;
import java.util.Optional;

import org.example.be_sp.entity.ChiTietSanPham;
import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    ArrayList<HoaDonChiTiet> findAllByIdHoaDonAndTrangThai(HoaDon idHoaDon, Boolean trangThai);

    Optional<HoaDonChiTiet> findFirstByIdHoaDonAndIdChiTietSanPhamAndDeletedFalse(HoaDon idHoaDon,
            ChiTietSanPham idChiTietSanPham);
}
