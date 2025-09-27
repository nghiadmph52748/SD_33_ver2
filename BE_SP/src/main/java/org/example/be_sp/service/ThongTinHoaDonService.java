package org.example.be_sp.service;

import org.example.be_sp.entity.ThongTinDonHang;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.ThongTinHoaDonRequest;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.model.response.ThongTinDonHangResponse;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.repository.ThongTinDonHangRepository;
import org.example.be_sp.repository.TrangThaiDonHangRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThongTinHoaDonService {
    @Autowired
    ThongTinDonHangRepository thongTinDonHangRepository;
    @Autowired
    HoaDonRepository hoaDonService;
    @Autowired
    TrangThaiDonHangRepository trangThaiDonHangService;
    public List<ThongTinDonHangResponse> getAll() {
        return thongTinDonHangRepository.findAll().stream().map(ThongTinDonHangResponse::new).toList();
    }
    public PagingResponse<ThongTinDonHangResponse> phanTrang(Integer no, Integer size) {
        Pageable page = PageRequest.of(no, size);
        return new PagingResponse<>(thongTinDonHangRepository.findAll(page).map(ThongTinDonHangResponse::new), no);
    }
    public ThongTinDonHangResponse getById(Integer id) {
        return thongTinDonHangRepository.findById(id).map(ThongTinDonHangResponse::new).orElseThrow(()-> new ApiException("Không tìm thấy thông tin đơn hàng","404" ));
    }
    public void add(ThongTinHoaDonRequest thongTinHoaDonRequest) {
        ThongTinDonHang ttdh = MapperUtils.map(thongTinHoaDonRequest, ThongTinDonHang.class);
        ttdh.setIdHoaDon(hoaDonService.getById(thongTinHoaDonRequest.getIdHoaDon()));
        ttdh.setIdTrangThaiDonHang(trangThaiDonHangService.getById(thongTinHoaDonRequest.getIdTrangThaiDonHang()));
        thongTinDonHangRepository.save(ttdh);
    }
    public void update(Integer id, ThongTinHoaDonRequest thongTinHoaDonRequest) {
        ThongTinDonHang ttdh = thongTinDonHangRepository.findById(id).orElseThrow(()-> new ApiException("Không tìm thấy thông tin đơn hàng","404" ));
        MapperUtils.mapToExisting(thongTinHoaDonRequest,ttdh);
        ttdh.setIdHoaDon(hoaDonService.getById(thongTinHoaDonRequest.getIdHoaDon()));
        ttdh.setIdTrangThaiDonHang(trangThaiDonHangService.getById(thongTinHoaDonRequest.getIdTrangThaiDonHang()));
        thongTinDonHangRepository.save(ttdh);
    }
    public void delete(Integer id) {
        ThongTinDonHang ttdh = thongTinDonHangRepository.findById(id).orElseThrow(()-> new ApiException("Không tìm thấy thông tin đơn hàng","404" ));
        ttdh.setDeleted(true);
        thongTinDonHangRepository.save(ttdh);
    }

}
