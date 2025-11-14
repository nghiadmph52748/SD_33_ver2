package org.example.be_sp.repository;

import org.example.be_sp.entity.GiaoCa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface GiaoCaRepository extends JpaRepository<GiaoCa, Long> {
    boolean existsByNguoiNhan_IdAndCaLamViec_IdAndThoiGianGiaoCaBetween(
            Integer nguoiNhanId, Long caLamViecId, LocalDateTime start, LocalDateTime end);

    boolean existsByNguoiGiao_IdAndCaLamViec_IdAndThoiGianGiaoCaBetween(
            Integer nguoiGiaoId, Long caLamViecId, LocalDateTime start, LocalDateTime end);

    Optional<GiaoCa> findByNguoiNhanIdAndTrangThaiXacNhan(Integer nhanVienId, String trangThaiXacNhan);
}

