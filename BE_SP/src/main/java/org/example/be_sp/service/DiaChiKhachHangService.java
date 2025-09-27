package org.example.be_sp.service;

import java.util.List;

import org.example.be_sp.entity.DiaChiKhachHang;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.DiaChiKhachHangRequest;
import org.example.be_sp.model.response.DiaChiKhachHangResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.DiaChiKhachHangRepository;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DiaChiKhachHangService {
    @Autowired
    DiaChiKhachHangRepository repository;
    @Autowired
    KhachHangRepository khachHangRepository;

    public List<DiaChiKhachHangResponse> getAll() {
        return repository.findAll().stream().map(DiaChiKhachHangResponse::new).toList();
    }

    public DiaChiKhachHangResponse getById(Integer id) {
        return repository.findById(id).map(DiaChiKhachHangResponse::new).orElseThrow(() -> new ApiException("Địa chỉ khách hàng không tồn tại", "404"));
    }

    public PagingResponse<DiaChiKhachHangResponse> paging(Integer page, Integer size) {
        return new PagingResponse<>(repository.findAll(PageRequest.of(page, size)).map(DiaChiKhachHangResponse::new), page);
    }

    public void add(DiaChiKhachHangRequest request){
        DiaChiKhachHang e = MapperUtils.map(request, DiaChiKhachHang.class);
        repository.save(e);
    }

    public void update(Integer id, DiaChiKhachHangRequest request){
        DiaChiKhachHang e = MapperUtils.map(request, DiaChiKhachHang.class);
        e.setId(id);
        e.setIdKhachHang(khachHangRepository.getKhachHangById(request.getIdKhachHang()));
        repository.save(e);
    }

    public void updateStatus(Integer id){
        DiaChiKhachHang e = repository.findById(id).orElseThrow(() -> new ApiException("Địa chỉ khách hàng không tồn tại", "404"));
        e.setDeleted(true);
        repository.save(e);
    }
}
