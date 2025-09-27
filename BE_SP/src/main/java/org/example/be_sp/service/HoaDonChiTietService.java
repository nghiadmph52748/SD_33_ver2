package org.example.be_sp.service;

import java.util.List;

import org.example.be_sp.entity.HoaDonChiTiet;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.HoaDonChiTietRequest;
import org.example.be_sp.model.response.HoaDonChiTietResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.ChiTietSanPhamRepository;
import org.example.be_sp.repository.HoaDonChiTietRepository;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class HoaDonChiTietService {
    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired
    private HoaDonRepository hoaDonService;
    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    public List<HoaDonChiTietResponse> getAll() {
        return hoaDonChiTietRepository.findAll().stream().map(HoaDonChiTietResponse::new).toList();
    }
    // Temporarily commented out to fix startup issue

    public PagingResponse<HoaDonChiTietResponse> phanTrang(Integer no, Integer size) {
        Pageable page = PageRequest.of(no, size);
        return new PagingResponse<>(hoaDonChiTietRepository.findAll(page).map(HoaDonChiTietResponse::new), no);
    }

    public HoaDonChiTietResponse getById(Integer id) {
        return hoaDonChiTietRepository.findById(id).map(HoaDonChiTietResponse::new).orElseThrow(()-> new ApiException("HoaDonChiTiet not found with id: " + id,"404"));
    }
    public void add(HoaDonChiTietRequest hoaDonChiTietRequest) {
        HoaDonChiTiet hdct = MapperUtils.map( hoaDonChiTietRequest, HoaDonChiTiet.class);
        hdct.setIdHoaDon(hoaDonService.getById(hoaDonChiTietRequest.getIdHoaDon()));
        hdct.setIdChiTietSanPham(chiTietSanPhamRepository.findChiTietSanPhamById(hoaDonChiTietRequest.getIdChiTietSanPham()));
        hoaDonChiTietRepository.save(hdct);
    }
    public void update(Integer id,HoaDonChiTietRequest hoaDonChiTietRequest) {
        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(id).orElseThrow(()-> new ApiException("HoaDonChiTiet not found with id: " + id,"404"));
        MapperUtils.mapToExisting(hoaDonChiTietRequest,hdct);
        hdct.setIdHoaDon(hoaDonService.getById(hoaDonChiTietRequest.getIdHoaDon()));
        hdct.setIdChiTietSanPham(chiTietSanPhamRepository.findChiTietSanPhamById(hoaDonChiTietRequest.getIdChiTietSanPham()));
        hoaDonChiTietRepository.save(hdct);
    }
    public void updateTrangThai(Integer id) {
        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(id).orElseThrow(()-> new ApiException("HoaDonChiTiet not found with id: " + id,"404"));
        hdct.setDeleted(true);
        hoaDonChiTietRepository.save(hdct);
    }

}
