package org.example.be_sp.repository;

import java.util.List;

import org.example.be_sp.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    SanPham findSanPhamById(Integer id);

    List<SanPham> findAllByDeleted(Boolean deleted);
    
    Page<SanPham> findAllByDeleted(Boolean deleted, Pageable pageable);
}
