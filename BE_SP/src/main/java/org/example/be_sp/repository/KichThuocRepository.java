package org.example.be_sp.repository;

import org.example.be_sp.entity.KichThuoc;
import org.example.be_sp.model.response.KichThuocResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KichThuocRepository extends JpaRepository<KichThuoc, Integer> {
    KichThuoc findKichThuocById(Integer id);

    List<KichThuoc> findAllByDeleted(Boolean deleted);
}
