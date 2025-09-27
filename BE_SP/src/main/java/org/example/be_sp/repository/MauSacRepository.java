package org.example.be_sp.repository;

import org.example.be_sp.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac, Integer> {
    MauSac findMauSacById(Integer id);
    @Query("select n from MauSac n where n.deleted = ?1")
    List<MauSac> findAllByDeletedFalse(Boolean deleted);
}
