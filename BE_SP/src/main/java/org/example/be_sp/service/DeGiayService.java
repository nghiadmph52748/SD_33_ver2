package org.example.be_sp.service;

import org.example.be_sp.entity.DeGiay;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.DeGiayRequest;
import org.example.be_sp.model.response.DeGiayResponse;
import org.example.be_sp.repository.DeGiayRepository;
import org.example.be_sp.util.GenericCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeGiayService extends GenericCrudService<DeGiay, Integer, DeGiayResponse, DeGiayRequest> {

    @Autowired
    private DeGiayRepository repository;
    public DeGiayService(Class<DeGiay> entity, Class<DeGiayResponse> deGiayResponseClass, Class<DeGiayRequest> deGiayRequestClass, JpaRepository<DeGiay, Integer> repository) {
        super(entity, deGiayResponseClass, deGiayRequestClass, repository);
    }

    public List<DeGiayResponse> getAllDeGiay() {
        return repository.findAllByDeleted(false).stream().map(DeGiayResponse::new).toList();
    }

    public void updateStatus(Integer id) {
        DeGiay deGiay = repository.findById(id).orElseThrow(() -> new ApiException("DeGiay not found","404"));
        deGiay.setDeleted(true);
        repository.save(deGiay);
    }
}
