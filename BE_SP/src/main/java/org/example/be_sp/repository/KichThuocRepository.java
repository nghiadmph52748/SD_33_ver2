package org.example.be_sp.repository;

import java.util.List;

import org.example.be_sp.entity.KichThuoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KichThuocRepository extends JpaRepository<KichThuoc, Integer> {

    KichThuoc findKichThuocById(Integer id);

    List<KichThuoc> findAllByDeleted(Boolean deleted);

    Page<KichThuoc> findAllByDeleted(Boolean deleted, Pageable pageable);
}
