package org.example.be_sp.repository;

import org.example.be_sp.entity.DotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, Integer> {
    DotGiamGia findDotGiamGiaById(Integer id);
}
