package org.example.be_sp.repository;

import org.example.be_sp.entity.DotGiamGiaHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DotGiamGiaHistoryRepository extends JpaRepository<DotGiamGiaHistory, Long> {
    
    /**
     * Find all history records for a promotion campaign, ordered by change date descending
     */
    List<DotGiamGiaHistory> findByIdDotGiamGiaOrderByNgayThayDoiDesc(Integer idDotGiamGia);
}
