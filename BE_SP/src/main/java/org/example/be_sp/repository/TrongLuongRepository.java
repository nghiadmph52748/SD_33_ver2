package org.example.be_sp.repository;

import java.util.List;

import org.example.be_sp.entity.TrongLuong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TrongLuongRepository extends JpaRepository<TrongLuong, Integer> {

    TrongLuong findTrongLuongById(Integer id);

    @Query("select n from TrongLuong n where n.deleted = ?1")
    List<TrongLuong> findByDeletedFalse(Boolean deleted);

    Page<TrongLuong> findAllByDeleted(Boolean deleted, Pageable pageable);
}
