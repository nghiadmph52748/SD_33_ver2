package org.example.be_sp.repository;

import org.example.be_sp.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    KhachHang findByEmail(String email);
    KhachHang findByTenTaiKhoan(String tenTaiKhoan);

    KhachHang getKhachHangById(Integer id);

    KhachHang getById(Integer id);

    KhachHang findKhachHangById(Integer id);

    boolean existsByTenTaiKhoan(String tenTaiKhoan);
}
