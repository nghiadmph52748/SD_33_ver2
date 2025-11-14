package org.example.be_sp.repository;

import org.example.be_sp.entity.CaLamViec;
import org.example.be_sp.entity.LichLamViec;
import org.example.be_sp.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface LichLamViecRepository extends JpaRepository<LichLamViec, Long> {


    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
            "FROM LichLamViec l " +
            "WHERE l.nhanVien = :nhanVien AND l.caLamViec = :caLamViec AND l.ngayLamViec = :ngayLamViec AND l.id <> :excludeId")
    boolean existsByNhanVienAndCaLamViecAndNgayLamViecAndIdNot(
            @Param("nhanVien") NhanVien nhanVien,
            @Param("caLamViec") CaLamViec caLamViec,
            @Param("ngayLamViec") LocalDate ngayLamViec,
            @Param("excludeId") Long excludeId
    );


}
