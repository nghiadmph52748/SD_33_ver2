package org.example.be_sp.service;

import java.util.List;

import org.example.be_sp.entity.MauSac;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.MauSacRequest;
import org.example.be_sp.model.response.MauSacResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.MauSacRepository;
import org.example.be_sp.util.GenericCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class MauSacService extends GenericCrudService<MauSac, Integer, MauSacResponse, MauSacRequest> {

    @Autowired
    MauSacRepository mauSacRepository;

    public MauSacService(Class<MauSac> entity, Class<MauSacResponse> mauSacResponseClass, Class<MauSacRequest> mauSacRequestClass, JpaRepository<MauSac, Integer> repository) {
        super(entity, mauSacResponseClass, mauSacRequestClass, repository);
    }

    public PagingResponse<MauSacResponse> pagingwithdeletedfalse(int page, int size) {
        return new PagingResponse<>(mauSacRepository.findAllByDeleted(false, PageRequest.of(page, size)).map(MauSacResponse::new), page);
    }

    public List<MauSacResponse> getAllMauSac() {
        List<MauSac> mauSacs = mauSacRepository.findAllByDeletedFalse(false);
        return mauSacs.stream().map(MauSacResponse::new).toList();
    }

    public void updateStatus(Integer id) {
        MauSac mauSac = mauSacRepository.findById(id).orElseThrow(() -> new ApiException("MauSac not found", "404"));
        mauSac.setDeleted(true);
        mauSacRepository.save(mauSac);
    }
}
