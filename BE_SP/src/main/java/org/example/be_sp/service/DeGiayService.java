package org.example.be_sp.service;

import java.util.List;

import org.example.be_sp.entity.DeGiay;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.DeGiayRequest;
import org.example.be_sp.model.response.DeGiayResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.DeGiayRepository;
import org.example.be_sp.util.GenericCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class DeGiayService extends GenericCrudService<DeGiay, Integer, DeGiayResponse, DeGiayRequest> {

    @Autowired
    private DeGiayRepository repository;

    public DeGiayService(Class<DeGiay> entity, Class<DeGiayResponse> deGiayResponseClass, Class<DeGiayRequest> deGiayRequestClass, JpaRepository<DeGiay, Integer> repository) {
        super(entity, deGiayResponseClass, deGiayRequestClass, repository);
    }

    public PagingResponse<DeGiayResponse> pagingwithdeletedfalse(int page, int size) {
        return new PagingResponse<>(repository.findAllByDeleted(false, PageRequest.of(page, size)).map(DeGiayResponse::new), page);
    }

    public List<DeGiayResponse> getAllDeGiay() {
        return repository.findAllByDeleted(false).stream().map(DeGiayResponse::new).toList();
    }

    public void updateStatus(Integer id) {
        DeGiay deGiay = repository.findById(id).orElseThrow(() -> new ApiException("DeGiay not found", "404"));
        deGiay.setDeleted(true);
        repository.save(deGiay);
    }
}
