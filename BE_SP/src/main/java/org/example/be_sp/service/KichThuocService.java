package org.example.be_sp.service;

import org.example.be_sp.entity.KichThuoc;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.KichThuocRequest;
import org.example.be_sp.model.response.KichThuocResponse;
import org.example.be_sp.repository.KichThuocRepository;
import org.example.be_sp.util.GenericCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KichThuocService extends GenericCrudService<KichThuoc, Integer, KichThuocResponse, KichThuocRequest> {
    @Autowired
    private KichThuocRepository repository;

    public KichThuocService(Class<KichThuoc> entity, Class<KichThuocResponse> kichThuocResponseClass, Class<KichThuocRequest> kichThuocRequestClass, JpaRepository<KichThuoc, Integer> repository) {
        super(entity, kichThuocResponseClass, kichThuocRequestClass, repository);
    }

    public List<KichThuocResponse> getAllKichThuoc() {
        return repository.findAllByDeleted(false).stream().map(KichThuocResponse::new).toList();
    }
    public void updateStatus(Integer id) {
        KichThuoc kichThuoc = repository.findById(id).orElseThrow(() -> new ApiException("KichThuoc not found","404"));
        kichThuoc.setDeleted(true);
        repository.save(kichThuoc);
    }

}
