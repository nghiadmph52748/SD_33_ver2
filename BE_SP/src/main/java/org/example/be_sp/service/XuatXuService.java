package org.example.be_sp.service;

import java.util.List;

import org.example.be_sp.entity.XuatXu;
import org.example.be_sp.model.request.XuatXuRequest;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.model.response.XuatXuResponse;
import org.example.be_sp.repository.XuatXuRepository;
import org.example.be_sp.util.GenericCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class XuatXuService extends GenericCrudService<XuatXu, Integer, XuatXuResponse, XuatXuRequest> {

    public XuatXuService(Class<XuatXu> entity, Class<XuatXuResponse> xuatXuResponseClass, Class<XuatXuRequest> xuatXuRequestClass, JpaRepository<XuatXu, Integer> repository) {
        super(entity, xuatXuResponseClass, xuatXuRequestClass, repository);
    }

    public PagingResponse<XuatXuResponse> pagingwithdeletedfalse(int page, int size) {
        return new PagingResponse<>(repository.findAllByDeleted(false, PageRequest.of(page, size)).map(XuatXuResponse::new), page);
    }

    public List<XuatXuResponse> getAllXuatXu() {
        List<XuatXu> xuatXus = repository.findByDeletedFalse(false);
        return xuatXus.stream().map(XuatXuResponse::new).toList();
    }

    @Autowired
    XuatXuRepository repository;

    public void updateStatus(Integer id) {
        XuatXu xuatXu = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy xuất xứ với id: " + id));
        xuatXu.setDeleted(true);
        repository.save(xuatXu);
    }
}
