package org.example.be_sp.repository;

import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.entity.PhieuGiamGia;
import org.example.be_sp.entity.PhieuGiamGiaCaNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhieuGiamGiaCaNhanRepository extends JpaRepository<PhieuGiamGiaCaNhan, Integer> {

    List<PhieuGiamGiaCaNhan> findByIdPhieuGiamGiaId(Integer idPhieuGiamGiaId);

    List<PhieuGiamGiaCaNhan> findAllByIdKhachHang(KhachHang idKhachHang);

    List<PhieuGiamGiaCaNhan> findAllByIdKhachHangAndDeletedAndTrangThai(KhachHang idKhachHang, Boolean deleted, Boolean trangThai);

    List<PhieuGiamGiaCaNhan> findAllByIdKhachHangIsNullAndDeletedAndTrangThai(Boolean deleted, Boolean trangThai);

    PhieuGiamGiaCaNhan findByIdKhachHangAndIdPhieuGiamGiaAndTrangThaiAndDeleted(KhachHang idKhachHang, PhieuGiamGia idPhieuGiamGia,
            Boolean trangThai, Boolean deleted);

    PhieuGiamGiaCaNhan findFirstByIdKhachHangIsNullAndIdPhieuGiamGiaAndTrangThaiAndDeleted(PhieuGiamGia idPhieuGiamGia,
            Boolean trangThai, Boolean deleted);
}
