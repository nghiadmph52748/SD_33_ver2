package org.example.be_sp.repository;

import java.util.List;

import org.example.be_sp.entity.ChiTietSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {

    ChiTietSanPham findChiTietSanPhamById(Integer id);

    List<ChiTietSanPham> findAllByDeleted(Boolean deleted);

    List<ChiTietSanPham> findAllByDeletedAndIdSanPham_Id(Boolean deleted, Integer idSanPhamId);

    Page<ChiTietSanPham> findAllByDeletedAndIdSanPham_Id(Boolean deleted, Integer idSanPhamId, Pageable pageable);

    Page<ChiTietSanPham> findAllByDeleted(Boolean deleted, Pageable pageable);
}
