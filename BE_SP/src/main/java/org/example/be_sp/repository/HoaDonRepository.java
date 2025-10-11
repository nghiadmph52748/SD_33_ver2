package org.example.be_sp.repository;

import java.util.Optional;

import org.example.be_sp.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    @Query("SELECT hd FROM HoaDon hd WHERE hd.id = :id")
    Optional<HoaDon> findHoaDonWithPayments(@Param("id") Integer id);

}

