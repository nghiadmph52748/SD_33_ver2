package org.example.be_sp.service;

import org.apache.catalina.mapper.Mapper;
import org.example.be_sp.entity.PhieuGiamGiaCaNhan;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.PhieuGiamGiaCaNhanRequest;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.model.response.PhieuGiamGiaCaNhanResponse;
import org.example.be_sp.model.response.PhieuGiamGiaResponse;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.repository.PhieuGiamGiaCaNhanRepository;
import org.example.be_sp.repository.PhieuGiamGiaRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhieuGiamGiaCaNhanService {
    @Autowired
    PhieuGiamGiaCaNhanRepository repository;
    @Autowired
    PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    KhachHangRepository khachHangRepository;

    public List<PhieuGiamGiaCaNhanResponse> getAllResponse(){
        return repository.findAll().stream().map(PhieuGiamGiaCaNhanResponse::new).toList();
    }

    public PhieuGiamGiaCaNhanResponse getByIdResponse(Integer id) {
        return new PhieuGiamGiaCaNhanResponse(repository.findById(id).orElseThrow(()-> new ApiException("Not found phieu giam gia ca nhan with id: " + id,"404")) );
    }

    public PagingResponse<PhieuGiamGiaCaNhanResponse> getAllResponsePaging(Integer page, Integer size){
        return new PagingResponse<>(repository.findAll(PageRequest.of(page, size)), page);
    }

    public void add(PhieuGiamGiaCaNhanRequest request){
        PhieuGiamGiaCaNhan e = MapperUtils.map(request, PhieuGiamGiaCaNhan.class);
        e.setIdPhieuGiamGia(phieuGiamGiaRepository.getById(request.getIdPhieuGiamGia()));
        e.setIdKhachHang(khachHangRepository.getById(request.getIdKhachHang()));
        repository.save(e);
    }

    public void update(Integer id, PhieuGiamGiaCaNhanRequest request) {
        PhieuGiamGiaCaNhan e = MapperUtils.map(request, PhieuGiamGiaCaNhan.class);
        e.setId(id);
        e.setIdPhieuGiamGia(phieuGiamGiaRepository.getById(request.getIdPhieuGiamGia()));
        e.setIdKhachHang(khachHangRepository.getById(request.getIdKhachHang()));
        repository.save(e);
    }

    public void updateStatus(Integer id) {
        PhieuGiamGiaCaNhan c = repository.findById(id).orElseThrow(()-> new ApiException("Not found phieu giam gia ca nhan with id: " + id,"404"));
        c.setDeleted(true);
        repository.save(c);
    }
}
