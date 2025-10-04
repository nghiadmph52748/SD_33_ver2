package org.example.be_sp.service;

import org.example.be_sp.entity.DotGiamGia;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.DotGiamGiaRequest;
import org.example.be_sp.model.response.DotGiamGiaResponse;
import org.example.be_sp.repository.DotGiamGiaRepository;
import org.example.be_sp.util.GenericCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class DotGiamGiaService extends GenericCrudService<DotGiamGia, Integer, DotGiamGiaResponse, DotGiamGiaRequest> {
    public DotGiamGiaService(Class<DotGiamGia> entity, Class<DotGiamGiaResponse> dotGiamGiaResponseClass,
            Class<DotGiamGiaRequest> dotGiamGiaRequestClass, JpaRepository<DotGiamGia, Integer> repository) {
        super(entity, dotGiamGiaResponseClass, dotGiamGiaRequestClass, repository);
    }

    @Autowired
    DotGiamGiaRepository repository;

    public void updateStatus(Integer id) {
        DotGiamGia e = repository.findById(id).orElseThrow(() -> new ApiException("DotGiamGia not found", "404"));
        e.setDeleted(true);
        repository.save(e);
    }

    public void delete(Integer id) {
        // Verify the entity exists before deleting
        if (!repository.existsById(id)) {
            throw new ApiException("DotGiamGia not found", "404");
        }
        // Hard delete from database
        repository.deleteById(id);
    }
}
