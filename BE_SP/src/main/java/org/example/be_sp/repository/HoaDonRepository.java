package org.example.be_sp.repository;

import org.example.be_sp.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    @Query("SELECT hd FROM HoaDon hd " +
            "LEFT JOIN FETCH hd.hinhThucThanhToans " +
            "LEFT JOIN FETCH hd.hoaDonChiTiets " +
            "WHERE hd.id = :id")
    Optional<HoaDon> findHoaDonWithPayments(@Param("id") Integer id);

}

