package org.example.be_sp.repository;

import org.example.be_sp.entity.HinhThucThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HinhThucChuyenKhoanRepository extends JpaRepository<HinhThucThanhToan, Integer> {
}
