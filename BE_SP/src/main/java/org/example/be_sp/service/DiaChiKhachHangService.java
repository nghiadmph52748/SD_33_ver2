package org.example.be_sp.service;

import java.util.List;

import org.example.be_sp.entity.DiaChiKhachHang;
import org.example.be_sp.entity.KhachHang;
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

    public void add(DiaChiKhachHangRequest request) {
        DiaChiKhachHang e = MapperUtils.map(request, DiaChiKhachHang.class);
        repository.save(e);
    }

    public void update(Integer id, DiaChiKhachHangRequest request) {
        DiaChiKhachHang e = MapperUtils.map(request, DiaChiKhachHang.class);
        e.setId(id);
        e.setIdKhachHang(khachHangRepository.getKhachHangById(request.getIdKhachHang()));
        repository.save(e);
    }

    public void updateStatus(Integer id) {
        DiaChiKhachHang e = repository.findById(id).orElseThrow(() -> new ApiException("Địa chỉ khách hàng không tồn tại", "404"));
        e.setDeleted(true);
        repository.save(e);
    }

    public DiaChiKhachHangResponse saveIfMissing(DiaChiKhachHangRequest request) {
        validateAddressRequest(request);

        Integer idKhachHang = request.getIdKhachHang();
        KhachHang khachHang = khachHangRepository.findById(idKhachHang)
                .orElseThrow(() -> new ApiException("Khách hàng không tồn tại", "404"));

        String thanhPho = normalize(request.getThanhPho());
        String quan = normalize(request.getQuan());
        String phuong = normalize(request.getPhuong());
        String diaChiCuThe = normalize(request.getDiaChiCuThe());

        return repository
                .findFirstByIdKhachHang_IdAndThanhPhoIgnoreCaseAndQuanIgnoreCaseAndPhuongIgnoreCaseAndDiaChiCuTheIgnoreCase(
                        idKhachHang,
                        thanhPho,
                        quan,
                        phuong,
                        diaChiCuThe)
                .map(DiaChiKhachHangResponse::new)
                .orElseGet(() -> {
                    DiaChiKhachHang entity = new DiaChiKhachHang();
                    entity.setIdKhachHang(khachHang);
                    entity.setTenDiaChi(resolveAddressName(request.getTenDiaChi(), khachHang.getTenKhachHang()));
                    entity.setThanhPho(thanhPho);
                    entity.setQuan(quan);
                    entity.setPhuong(phuong);
                    entity.setDiaChiCuThe(diaChiCuThe);
                    entity.setDeleted(false);
                    entity.setTrangThai(true);
                    entity.setMacDinh(false);
                    return new DiaChiKhachHangResponse(repository.save(entity));
                });
    }

    private void validateAddressRequest(DiaChiKhachHangRequest request) {
        if (request == null || request.getIdKhachHang() == null) {
            throw new ApiException("Thiếu thông tin khách hàng", "400");
        }
        if (isBlank(request.getThanhPho()) || isBlank(request.getQuan()) || isBlank(request.getPhuong()) || isBlank(request.getDiaChiCuThe())) {
            throw new ApiException("Địa chỉ giao hàng không hợp lệ", "400");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String resolveAddressName(String requestedName, String customerName) {
        if (!isBlank(requestedName)) {
            return requestedName.trim();
        }
        if (!isBlank(customerName)) {
            return String.format("Địa chỉ %s", customerName.trim());
        }
        return "Địa chỉ POS";
    }
}
