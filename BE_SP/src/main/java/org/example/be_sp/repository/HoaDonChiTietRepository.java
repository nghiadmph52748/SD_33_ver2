package org.example.be_sp.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.be_sp.entity.ChiTietSanPham;
import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    ArrayList<HoaDonChiTiet> findAllByIdHoaDonAndTrangThai(HoaDon idHoaDon, Boolean trangThai);

    Optional<HoaDonChiTiet> findFirstByIdHoaDonAndIdChiTietSanPhamAndDeletedFalse(HoaDon idHoaDon,
            ChiTietSanPham idChiTietSanPham);

    @Query(value = "WITH SalesSummary AS (" +
            "SELECT ctsp.id_san_pham, " +
            "SUM(hdct.so_luong) as total_sold, " +
            "ROW_NUMBER() OVER (ORDER BY SUM(hdct.so_luong) DESC) as rn " +
            "FROM hoa_don_chi_tiet hdct " +
            "INNER JOIN chi_tiet_san_pham ctsp ON hdct.id_chi_tiet_san_pham = ctsp.id " +
            "INNER JOIN hoa_don hd ON hdct.id_hoa_don = hd.id " +
            "WHERE hdct.deleted = 0 AND hdct.trang_thai = 1 " +
            "AND hd.deleted = 0 " +
            "GROUP BY ctsp.id_san_pham " +
            ") " +
            "SELECT id_san_pham, total_sold " +
            "FROM SalesSummary " +
            "WHERE rn <= :limit " +
            "ORDER BY total_sold DESC", nativeQuery = true)
    List<Object[]> findBestSellingProductIds(@Param("limit") int limit);
}
