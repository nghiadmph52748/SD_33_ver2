package org.example.be_sp.repository;

import org.example.be_sp.entity.NhaSanXuat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NhaSanXuatRepository extends JpaRepository<NhaSanXuat, Integer> {
    NhaSanXuat findNhaSanXuatById(Integer id);
    @Query("SELECT n FROM NhaSanXuat n WHERE n.deleted = ?1")
    List<NhaSanXuat> findByDeletedFalse(Boolean deleted);
}
