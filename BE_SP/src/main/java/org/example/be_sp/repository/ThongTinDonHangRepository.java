package org.example.be_sp.repository;

import java.util.List;
import java.util.Optional;

import org.example.be_sp.entity.ThongTinDonHang;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ThongTinDonHangRepository extends JpaRepository<ThongTinDonHang, Integer> {

    /**
     * Tìm thông tin đơn hàng theo ID hóa đơn
     */
    @Query("SELECT t FROM ThongTinDonHang t WHERE t.idHoaDon.id = :hoaDonId AND t.deleted = false AND (t.idTrangThaiDonHang.id IS NULL OR t.idTrangThaiDonHang.id <> 3) ORDER BY t.thoiGian DESC")
    List<ThongTinDonHang> findByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    /**
     * Tìm thông tin đơn hàng mới nhất theo ID hóa đơn
     */
    @Query("SELECT t FROM ThongTinDonHang t WHERE t.idHoaDon.id = :hoaDonId AND t.deleted = false AND (t.idTrangThaiDonHang.id IS NULL OR t.idTrangThaiDonHang.id <> 3) ORDER BY t.thoiGian DESC")
    List<ThongTinDonHang> findLatestByHoaDonId(@Param("hoaDonId") Integer hoaDonId, Pageable pageable);

    /**
     * Tìm thông tin đơn hàng mới nhất theo ID hóa đơn (không dùng Pageable)
     */
    @Query("SELECT t FROM ThongTinDonHang t WHERE t.idHoaDon.id = :hoaDonId AND t.deleted = false AND (t.idTrangThaiDonHang.id IS NULL OR t.idTrangThaiDonHang.id <> 3) ORDER BY t.thoiGian DESC")
    List<ThongTinDonHang> findByHoaDonIdOrderByThoiGianDesc(@Param("hoaDonId") Integer hoaDonId);

    /**
     * Tìm thông tin đơn hàng mới nhất theo ID hóa đơn (chỉ lấy 1 record)
     */
    default Optional<ThongTinDonHang> findLatestByHoaDonId(Integer hoaDonId) {
        List<ThongTinDonHang> results = findLatestByHoaDonId(hoaDonId, Pageable.ofSize(1));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Kiểm tra xem đơn hàng đã có record với trạng thái thay đổi địa chỉ (id =
     * 8) chưa
     */
    @Query("SELECT COUNT(t) > 0 FROM ThongTinDonHang t WHERE t.idHoaDon.id = :hoaDonId AND t.idTrangThaiDonHang.id = :statusId AND t.deleted = false")
    boolean existsByHoaDonIdAndStatusId(@Param("hoaDonId") Integer hoaDonId, @Param("statusId") Integer statusId);

    /**
     * Kiểm tra xem đơn hàng đã sở hữu ghi chú chứa từ khóa nhất định (ví dụ:
     * thay đổi địa chỉ giao hàng)
     */
    @Query("SELECT COUNT(t) > 0 FROM ThongTinDonHang t WHERE t.idHoaDon.id = :hoaDonId AND t.deleted = false AND LOWER(COALESCE(t.ghiChu, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    boolean existsByHoaDonIdAndGhiChuKeyword(@Param("hoaDonId") Integer hoaDonId, @Param("keyword") String keyword);
}
