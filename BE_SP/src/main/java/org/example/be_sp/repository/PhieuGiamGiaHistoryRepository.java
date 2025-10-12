package org.example.be_sp.repository;

import org.example.be_sp.entity.PhieuGiamGiaHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhieuGiamGiaHistoryRepository extends JpaRepository<PhieuGiamGiaHistory, Long> {

    /**
     * Find all history records for a specific coupon, ordered by date descending (newest first)
     * @param idPhieuGiamGia The coupon ID
     * @return List of history records
     */
    List<PhieuGiamGiaHistory> findByIdPhieuGiamGiaOrderByNgayThayDoiDesc(Integer idPhieuGiamGia);
}
