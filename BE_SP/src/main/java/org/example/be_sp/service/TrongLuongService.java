package org.example.be_sp.service;

import org.example.be_sp.entity.TrongLuong;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.TrongLuongRequest;
import org.example.be_sp.model.response.TrongLuongResponse;
import org.example.be_sp.repository.TrongLuongRepository;
import org.example.be_sp.util.GenericCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrongLuongService extends GenericCrudService<TrongLuong, Integer, TrongLuongResponse, TrongLuongRequest> {
    @Autowired
    private TrongLuongRepository repository;
    public TrongLuongService(Class<TrongLuong> entity, Class<TrongLuongResponse> trongLuongResponseClass, Class<TrongLuongRequest> trongLuongRequestClass, JpaRepository<TrongLuong, Integer> repository) {
        super(entity, trongLuongResponseClass, trongLuongRequestClass, repository);
    }

    public List<TrongLuongResponse> getAllTrongLuong() {
        List<TrongLuong> list = repository.findByDeletedFalse(false);
        return list.stream().map(TrongLuongResponse::new).toList();
    }

    public void updateStatus(Integer id) {
        TrongLuong trongLuong = repository.findById(id).orElseThrow(() -> new ApiException("TrongLuong not found","404"));
        trongLuong.setDeleted(true);
        repository.save(trongLuong);
    }
}
