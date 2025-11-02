package org.example.be_sp.repository;

import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {


    ArrayList<HoaDonChiTiet> findAllByIdHoaDonAndTrangThai(HoaDon idHoaDon, Boolean trangThai);
}
