package org.example.be_sp.repository;

import org.example.be_sp.entity.TimelineDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimelineDonHangRepository extends JpaRepository<TimelineDonHang, Integer> {

}
