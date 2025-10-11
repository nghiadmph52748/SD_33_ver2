package org.example.be_sp.repository;

import org.example.be_sp.entity.ThongTinDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThongTinDonHangRepository extends JpaRepository<ThongTinDonHang, Integer> {
    
    /**
     * Tìm thông tin đơn hàng theo ID hóa đơn
     */
    @Query("SELECT t FROM ThongTinDonHang t WHERE t.idHoaDon.id = :hoaDonId AND t.deleted = false ORDER BY t.thoiGian DESC")
    List<ThongTinDonHang> findByHoaDonId(@Param("hoaDonId") Integer hoaDonId);
    
    /**
     * Tìm thông tin đơn hàng mới nhất theo ID hóa đơn
     */
    @Query("SELECT t FROM ThongTinDonHang t WHERE t.idHoaDon.id = :hoaDonId AND t.deleted = false ORDER BY t.thoiGian DESC")
    List<ThongTinDonHang> findLatestByHoaDonId(@Param("hoaDonId") Integer hoaDonId, Pageable pageable);
    
    /**
     * Tìm thông tin đơn hàng mới nhất theo ID hóa đơn (chỉ lấy 1 record)
     */
    default Optional<ThongTinDonHang> findLatestByHoaDonId(Integer hoaDonId) {
        List<ThongTinDonHang> results = findLatestByHoaDonId(hoaDonId, Pageable.ofSize(1));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
