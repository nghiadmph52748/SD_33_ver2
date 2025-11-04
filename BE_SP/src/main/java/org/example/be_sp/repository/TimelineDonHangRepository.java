package org.example.be_sp.repository;

import java.util.List;

import org.example.be_sp.entity.TimelineDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TimelineDonHangRepository extends JpaRepository<TimelineDonHang, Integer> {
    
    /**
     * Tìm timeline theo ID hóa đơn
     */
    @Query("SELECT t FROM TimelineDonHang t WHERE t.idHoaDon.id = :hoaDonId AND (t.deleted = false OR t.deleted IS NULL) ORDER BY t.thoiGian ASC")
    List<TimelineDonHang> findByHoaDonId(@Param("hoaDonId") Integer hoaDonId);
}
