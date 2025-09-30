package org.example.be_sp.repository;

import org.example.be_sp.entity.DeGiay;
import org.example.be_sp.model.response.DeGiayResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeGiayRepository extends JpaRepository<DeGiay, Integer> {

    DeGiay findDeGiayById(Integer id);

    List<DeGiay> findAllByDeleted(Boolean deleted);

    Page<DeGiay> findAllByDeleted(Boolean deleted, Pageable pageable);
}
