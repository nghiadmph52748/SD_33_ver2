package org.example.be_sp.repository;

import java.util.List;

import org.example.be_sp.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {
    @Override
    PhieuGiamGia getById(Integer id);
    
    @Query("select n from PhieuGiamGia n where n.id = ?1")
    PhieuGiamGia findPhieuGiamGiaById(Integer id);
    
    @Query("select n from PhieuGiamGia n where n.deleted = ?1 and n.trangThai = ?2 and n.loaiPhieuGiamGia = ?3")
    List<PhieuGiamGia> findAllByDeletedFalseAndTrangThaiTrueAndLoaiPhieuGiamGiaTrue(Boolean deleted, Boolean trangThai, Boolean loaiPhieuGiamGia);
}
