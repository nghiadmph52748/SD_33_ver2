package org.example.be_sp.repository;

import java.util.List;

import org.example.be_sp.entity.XuatXu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface XuatXuRepository extends JpaRepository<XuatXu, Integer> {

    XuatXu findXuatXuById(Integer id);

    @Query("SELECT x FROM XuatXu x WHERE x.deleted = ?1")
    List<XuatXu> findByDeletedFalse(Boolean deleted);

    Page<XuatXu> findAllByDeleted(Boolean deleted, Pageable pageable);
}
