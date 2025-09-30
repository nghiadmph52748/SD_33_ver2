package org.example.be_sp.repository;

import java.util.List;

import org.example.be_sp.entity.MauSac;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac, Integer> {

    MauSac findMauSacById(Integer id);

    @Query("select n from MauSac n where n.deleted = ?1")
    List<MauSac> findAllByDeletedFalse(Boolean deleted);

    Page<MauSac> findAllByDeleted(Boolean deleted, Pageable pageable);
}
