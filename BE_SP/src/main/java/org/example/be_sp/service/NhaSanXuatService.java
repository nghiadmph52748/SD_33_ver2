package org.example.be_sp.service;

import org.example.be_sp.entity.NhaSanXuat;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.NhaSanXuatRequest;
import org.example.be_sp.model.response.NhaSanXuatResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.NhaSanXuatRepository;
import org.example.be_sp.util.GenericCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NhaSanXuatService extends GenericCrudService<NhaSanXuat, Integer, NhaSanXuatResponse, NhaSanXuatRequest> {
    @Autowired
    private NhaSanXuatRepository nhaSanXuatRepository;
    public NhaSanXuatService(Class<NhaSanXuat> entity, Class<NhaSanXuatResponse> nhaSanXuatResponseClass, Class<NhaSanXuatRequest> nhaSanXuatRequestClass, JpaRepository<NhaSanXuat, Integer> nhaSanXuatRepository) {
        super(entity, nhaSanXuatResponseClass, nhaSanXuatRequestClass, nhaSanXuatRepository);
    }

    public PagingResponse<NhaSanXuatResponse> getNhaSanXuatPagingFalse(Integer page, Integer size) {
        return new PagingResponse<>(nhaSanXuatRepository.findAllByDeleted(false, PageRequest.of(page,size)).map(NhaSanXuatResponse::new), page);
    }

    public List<NhaSanXuatResponse> getAllNhaSanXuat() {
        List<NhaSanXuat> list = nhaSanXuatRepository.findByDeletedFalse(false);
        return list.stream().map(NhaSanXuatResponse::new).toList();
    }

    public void updateStatus(Integer id) {
        NhaSanXuat nhaSanXuat = nhaSanXuatRepository.findById(id).orElseThrow(() -> new ApiException("NhaSanXuat not found", "404"));
        nhaSanXuat.setDeleted(true);
        nhaSanXuatRepository.save(nhaSanXuat);
    }

}
