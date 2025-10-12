package org.example.be_sp.repository;

import org.example.be_sp.entity.ChiTietDotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDotGiamGiaRepository extends JpaRepository<ChiTietDotGiamGia, Integer> {
    List<ChiTietDotGiamGia> findByIdDotGiamGia_IdAndDeletedFalse(Integer idDotGiamGia);
}
