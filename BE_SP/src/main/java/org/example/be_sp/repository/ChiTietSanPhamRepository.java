package org.example.be_sp.repository;

import java.util.List;
import java.util.Optional;

import org.example.be_sp.entity.ChiTietSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {

    ChiTietSanPham findChiTietSanPhamById(Integer id);

    List<ChiTietSanPham> findAllByDeleted(Boolean deleted);

    List<ChiTietSanPham> findAllByDeletedAndIdSanPham_Id(Boolean deleted, Integer idSanPhamId);

    Page<ChiTietSanPham> findAllByDeletedAndIdSanPham_Id(Boolean deleted, Integer idSanPhamId, Pageable pageable);

    Page<ChiTietSanPham> findAllByDeleted(Boolean deleted, Pageable pageable);

    // Custom queries with JOIN FETCH to eagerly load associations including discount details
    @Query("SELECT DISTINCT ctsp FROM ChiTietSanPham ctsp " +
           "LEFT JOIN FETCH ctsp.idSanPham sp " +
           "LEFT JOIN FETCH sp.idNhaSanXuat " +
           "LEFT JOIN FETCH sp.idXuatXu " +
           "LEFT JOIN FETCH ctsp.idMauSac " +
           "LEFT JOIN FETCH ctsp.idKichThuoc " +
           "LEFT JOIN FETCH ctsp.idDeGiay " +
           "LEFT JOIN FETCH ctsp.idChatLieu " +
           "LEFT JOIN FETCH ctsp.idTrongLuong " +
           "LEFT JOIN FETCH ctsp.chiTietSanPhamAnhs ctspa " +
           "LEFT JOIN FETCH ctspa.idAnhSanPham " +
           "LEFT JOIN FETCH ctsp.chiTietDotGiamGias ctdgg " +
           "LEFT JOIN FETCH ctdgg.idDotGiamGia " +
           "WHERE ctsp.deleted = :deleted")
    List<ChiTietSanPham> findAllByDeletedWithDetails(@Param("deleted") Boolean deleted);

    @Query("SELECT DISTINCT ctsp FROM ChiTietSanPham ctsp " +
           "LEFT JOIN FETCH ctsp.idSanPham sp " +
           "LEFT JOIN FETCH sp.idNhaSanXuat " +
           "LEFT JOIN FETCH sp.idXuatXu " +
           "LEFT JOIN FETCH ctsp.idMauSac " +
           "LEFT JOIN FETCH ctsp.idKichThuoc " +
           "LEFT JOIN FETCH ctsp.idDeGiay " +
           "LEFT JOIN FETCH ctsp.idChatLieu " +
           "LEFT JOIN FETCH ctsp.idTrongLuong " +
           "LEFT JOIN FETCH ctsp.chiTietSanPhamAnhs ctspa " +
           "LEFT JOIN FETCH ctspa.idAnhSanPham " +
           "LEFT JOIN FETCH ctsp.chiTietDotGiamGias ctdgg " +
           "LEFT JOIN FETCH ctdgg.idDotGiamGia " +
           "WHERE ctsp.deleted = :deleted AND ctsp.idSanPham.id = :idSanPham")
    List<ChiTietSanPham> findAllByDeletedAndIdSanPhamIdWithDetails(@Param("deleted") Boolean deleted, @Param("idSanPham") Integer idSanPham);

    @Query("SELECT DISTINCT ctsp FROM ChiTietSanPham ctsp " +
           "LEFT JOIN FETCH ctsp.idSanPham sp " +
           "LEFT JOIN FETCH sp.idNhaSanXuat " +
           "LEFT JOIN FETCH sp.idXuatXu " +
           "LEFT JOIN FETCH ctsp.idMauSac " +
           "LEFT JOIN FETCH ctsp.idKichThuoc " +
           "LEFT JOIN FETCH ctsp.idDeGiay " +
           "LEFT JOIN FETCH ctsp.idChatLieu " +
           "LEFT JOIN FETCH ctsp.idTrongLuong " +
           "LEFT JOIN FETCH ctsp.chiTietSanPhamAnhs ctspa " +
           "LEFT JOIN FETCH ctspa.idAnhSanPham " +
           "LEFT JOIN FETCH ctsp.chiTietDotGiamGias ctdgg " +
           "LEFT JOIN FETCH ctdgg.idDotGiamGia " +
           "WHERE ctsp.id = :id")
    Optional<ChiTietSanPham> findByIdWithDetails(@Param("id") Integer id);
}
