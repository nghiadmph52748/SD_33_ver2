package org.example.be_sp.service;

import java.util.List;

import org.example.be_sp.entity.ChiTietSanPhamAnh;
import org.example.be_sp.model.request.ChiTietSanPhamAnhListRequest;
import org.example.be_sp.model.response.ChiTietSanPhamAnhResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.AnhSanPhamRepository;
import org.example.be_sp.repository.ChiTietAnhSanPhamRepository;
import org.example.be_sp.repository.ChiTietSanPhamRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ChiTietSanPhamAnhService {
    @Autowired
    ChiTietAnhSanPhamRepository repository;
    @Autowired
    ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired
    AnhSanPhamRepository anhSanPhamRepository;

    public List<ChiTietSanPhamAnhResponse> getAll() {
        return repository.findAllByDeleted(false).stream().map(ChiTietSanPhamAnhResponse::new).toList();
    }

    public ChiTietSanPhamAnhResponse getById(Integer id) {
        return repository.findById(id)
                .map(ChiTietSanPhamAnhResponse::new)
                .orElseThrow(() -> new RuntimeException("ChiTietSanPhamAnh not found with id: " + id));
    }

    public PagingResponse<ChiTietSanPhamAnhResponse> paging(Integer page, Integer size) {
        return new PagingResponse<>(repository.findAll(PageRequest.of(page, size)).map(ChiTietSanPhamAnhResponse::new), page);
    }

    public void add(ChiTietSanPhamAnhListRequest request) {
        try {
            // Kiểm tra dữ liệu đầu vào
            if (request == null) {
                throw new RuntimeException("Request không được null");
            }
            if (request.getIdChiTietSanPham() == null) {
                throw new RuntimeException("ID chi tiết sản phẩm không được null");
            }
            if (request.getIdAnhSanPhamList() == null) {
                throw new RuntimeException("Danh sách ID ảnh sản phẩm không được null");
            }
            if (request.getIdAnhSanPhamList().isEmpty()) {
                throw new RuntimeException("Danh sách ID ảnh sản phẩm không được rỗng");
            }

            // Kiểm tra chi tiết sản phẩm có tồn tại không
            var chiTietSanPham = chiTietSanPhamRepository.findChiTietSanPhamById(request.getIdChiTietSanPham());
            if (chiTietSanPham == null) {
                throw new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + request.getIdChiTietSanPham());
            }

            for (Integer idAnhSanPham : request.getIdAnhSanPhamList()) {
                // Kiểm tra ID ảnh không null
                if (idAnhSanPham == null) {
                    throw new RuntimeException("ID ảnh sản phẩm không được null trong danh sách");
                }

                // Kiểm tra ảnh sản phẩm có tồn tại không
                var anhSanPham = anhSanPhamRepository.findAnhSanPhamById(idAnhSanPham);
                if (anhSanPham == null) {
                    throw new RuntimeException("Không tìm thấy ảnh sản phẩm với ID: " + idAnhSanPham);
                }

                ChiTietSanPhamAnh e = new ChiTietSanPhamAnh();
                e.setIdChiTietSanPham(chiTietSanPham);
                e.setIdAnhSanPham(anhSanPham);
                e.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
                e.setDeleted(request.getDeleted() != null ? request.getDeleted() : false);
                repository.save(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void update(Integer id, ChiTietSanPhamAnhListRequest request) {
        ChiTietSanPhamAnh existing = MapperUtils.map(request, ChiTietSanPhamAnh.class);
        existing.setId(id);
        existing.setIdChiTietSanPham(chiTietSanPhamRepository.findChiTietSanPhamById(request.getIdChiTietSanPham()));
        existing.setIdAnhSanPham(anhSanPhamRepository.findAnhSanPhamById(request.getIdAnhSanPhamList().get(0)));
        repository.save(existing);
    }

    public void updateStatus(Integer id){
        try {
            ChiTietSanPhamAnh existing = repository.findById(id).orElseThrow(() -> new RuntimeException("ChiTietSanPhamAnh not found with id: " + id));
            existing.setDeleted(true);
            existing.setTrangThai(false); // Set trangThai = false khi soft delete
            repository.save(existing);
        } catch (Exception e) {
            throw e;
        }
    }
}
