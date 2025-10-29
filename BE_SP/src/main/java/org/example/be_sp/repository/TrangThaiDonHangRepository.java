package org.example.be_sp.repository;

import org.example.be_sp.entity.TrangThaiDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrangThaiDonHangRepository extends JpaRepository<TrangThaiDonHang, Integer> {
}

