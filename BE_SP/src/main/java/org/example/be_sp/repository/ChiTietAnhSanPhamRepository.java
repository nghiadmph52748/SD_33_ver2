package org.example.be_sp.repository;

import java.util.List;

import org.example.be_sp.entity.ChiTietSanPhamAnh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChiTietAnhSanPhamRepository extends JpaRepository<ChiTietSanPhamAnh, Integer> {
    @Query("select n from ChiTietSanPhamAnh n where n.deleted = ?1")
    List<ChiTietSanPhamAnh> findAllByDeleted(Boolean deleted);
}
