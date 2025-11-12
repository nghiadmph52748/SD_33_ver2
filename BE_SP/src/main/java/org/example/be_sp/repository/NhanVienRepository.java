package org.example.be_sp.repository;

import org.example.be_sp.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    Optional<NhanVien> findByEmail(String email);

    Optional<NhanVien> findByTenTaiKhoan(String tenTaiKhoan);


    boolean existsByEmail(String email);

    boolean existsByTenTaiKhoan(String tenTaiKhoan);


    NhanVien getById(Integer id);

    Optional<NhanVien> findByResetToken(String resetToken);

}
