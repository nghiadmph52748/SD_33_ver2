package org.example.be_sp.repository;

import org.example.be_sp.entity.ChiTietPhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietPhieuGiamGiaRepository extends JpaRepository<ChiTietPhieuGiamGia, Integer> {
    
    @Query("SELECT c FROM ChiTietPhieuGiamGia c WHERE c.idPhieuGiamGia.id = :idPhieuGiamGia AND c.deleted = false")
    List<ChiTietPhieuGiamGia> findByIdPhieuGiamGia(@Param("idPhieuGiamGia") Integer idPhieuGiamGia);
    
    @Modifying
    @Query("UPDATE ChiTietPhieuGiamGia c SET c.deleted = true WHERE c.idPhieuGiamGia.id = :idPhieuGiamGia")
    void deleteByIdPhieuGiamGia(@Param("idPhieuGiamGia") Integer idPhieuGiamGia);
    
    @Query("SELECT c FROM ChiTietPhieuGiamGia c WHERE c.idChiTietSanPham.id = :idChiTietSanPham AND c.deleted = false")
    List<ChiTietPhieuGiamGia> findByIdChiTietSanPham(@Param("idChiTietSanPham") Integer idChiTietSanPham);
}

