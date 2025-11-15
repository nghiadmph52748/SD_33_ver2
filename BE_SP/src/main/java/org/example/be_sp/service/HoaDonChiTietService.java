package org.example.be_sp.service;

import java.util.List;

import org.example.be_sp.entity.ChiTietSanPham;
import org.example.be_sp.entity.HoaDonChiTiet;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.HoaDonChiTietRequest;
import org.example.be_sp.model.response.HoaDonChiTietResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.ChiTietSanPhamRepository;
import org.example.be_sp.repository.HoaDonChiTietRepository;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HoaDonChiTietService {

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired
    private HoaDonRepository hoaDonService;
    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired
    private ChiTietSanPhamService chiTietSanPhamService;

    public List<HoaDonChiTietResponse> getAll() {
        return hoaDonChiTietRepository.findAll().stream().map(HoaDonChiTietResponse::new).toList();
    }
    // Temporarily commented out to fix startup issue

    public PagingResponse<HoaDonChiTietResponse> phanTrang(Integer no, Integer size) {
        Pageable page = PageRequest.of(no, size);
        return new PagingResponse<>(hoaDonChiTietRepository.findAll(page).map(HoaDonChiTietResponse::new), no);
    }

    public HoaDonChiTietResponse getById(Integer id) {
        return hoaDonChiTietRepository.findById(id).map(HoaDonChiTietResponse::new).orElseThrow(() -> new ApiException("HoaDonChiTiet not found with id: " + id, "404"));
    }

    @Transactional
    public void add(HoaDonChiTietRequest hoaDonChiTietRequest) {
        HoaDonChiTiet hdct = MapperUtils.map(hoaDonChiTietRequest, HoaDonChiTiet.class);
        hdct.setIdHoaDon(hoaDonService.getById(hoaDonChiTietRequest.getIdHoaDon()));

        // Accept both idChiTietSanPham and idBienTheSanPham from FE
        Integer ctspId = hoaDonChiTietRequest.getIdChiTietSanPham() != null
                ? hoaDonChiTietRequest.getIdChiTietSanPham()
                : hoaDonChiTietRequest.getIdBienTheSanPham();
        if (ctspId == null) {
            throw new ApiException("Thiếu id chi tiết sản phẩm", "400");
        }
        var ctsp = chiTietSanPhamRepository.findChiTietSanPhamById(ctspId);
        if (ctsp == null) {
            throw new ApiException("Không tìm thấy chi tiết sản phẩm với id: " + ctspId, "404");
        }
        hdct.setIdChiTietSanPham(ctsp);

        // Validate quantity and update stock
        Integer qty = hoaDonChiTietRequest.getSoLuong();
        if (qty == null || qty <= 0) {
            throw new ApiException("Số lượng phải lớn hơn 0", "400");
        }
        Integer currentStock = ctsp.getSoLuong() == null ? 0 : ctsp.getSoLuong();
        if (currentStock < qty) {
            throw new ApiException("Số lượng tồn kho không đủ", "400");
        }
        int newStock = currentStock - qty;
        ctsp.setSoLuong(newStock);
        if (newStock <= 0) {
            ctsp.setTrangThai(false); // hết hàng
        }
        chiTietSanPhamRepository.save(ctsp);

        // 🔄 Auto-update variant status immediately when quantity changes
        chiTietSanPhamService.updateVariantStatusByQuantity(ctsp);

        // Tự động điền tên sản phẩm chi tiết và mã sản phẩm chi tiết
        if (hdct.getIdChiTietSanPham() != null) {
            if (hoaDonChiTietRequest.getTenSanPhamChiTiet() == null || hoaDonChiTietRequest.getTenSanPhamChiTiet().trim().isEmpty()) {
                // Tạo tên sản phẩm chi tiết từ các thuộc tính
                String tenSanPhamChiTiet = buildTenSanPhamChiTiet(hdct.getIdChiTietSanPham());
                hdct.setTenSanPhamChiTiet(tenSanPhamChiTiet);
            }

            if (hoaDonChiTietRequest.getMaSanPhamChiTiet() == null || hoaDonChiTietRequest.getMaSanPhamChiTiet().trim().isEmpty()) {
                hdct.setMaSanPhamChiTiet(hdct.getIdChiTietSanPham().getMaChiTietSanPham());
            }
        }

        hoaDonChiTietRepository.save(hdct);
    }

    public void update(Integer id, HoaDonChiTietRequest hoaDonChiTietRequest) {
        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(id).orElseThrow(() -> new ApiException("HoaDonChiTiet not found with id: " + id, "404"));
        MapperUtils.mapToExisting(hoaDonChiTietRequest, hdct);
        hdct.setIdHoaDon(hoaDonService.getById(hoaDonChiTietRequest.getIdHoaDon()));
        hdct.setIdChiTietSanPham(chiTietSanPhamRepository.findChiTietSanPhamById(hoaDonChiTietRequest.getIdChiTietSanPham()));

        // Tự động điền tên sản phẩm chi tiết và mã sản phẩm chi tiết
        if (hdct.getIdChiTietSanPham() != null) {
            if (hoaDonChiTietRequest.getTenSanPhamChiTiet() == null || hoaDonChiTietRequest.getTenSanPhamChiTiet().trim().isEmpty()) {
                // Tạo tên sản phẩm chi tiết từ các thuộc tính
                String tenSanPhamChiTiet = buildTenSanPhamChiTiet(hdct.getIdChiTietSanPham());
                hdct.setTenSanPhamChiTiet(tenSanPhamChiTiet);
            }

            if (hoaDonChiTietRequest.getMaSanPhamChiTiet() == null || hoaDonChiTietRequest.getMaSanPhamChiTiet().trim().isEmpty()) {
                hdct.setMaSanPhamChiTiet(hdct.getIdChiTietSanPham().getMaChiTietSanPham());
            }
        }

        hoaDonChiTietRepository.save(hdct);
    }

    public void updateTrangThai(Integer id) {
        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(id).orElseThrow(() -> new ApiException("HoaDonChiTiet not found with id: " + id, "404"));
        hdct.setDeleted(true);
        hoaDonChiTietRepository.save(hdct);
    }

    /**
     * Tạo tên sản phẩm chi tiết từ các thuộc tính của ChiTietSanPham
     */
    private String buildTenSanPhamChiTiet(ChiTietSanPham chiTietSanPham) {
        StringBuilder tenSanPhamChiTiet = new StringBuilder();

        // Tên sản phẩm chính
        if (chiTietSanPham.getIdSanPham() != null && chiTietSanPham.getIdSanPham().getTenSanPham() != null) {
            tenSanPhamChiTiet.append(chiTietSanPham.getIdSanPham().getTenSanPham());
        }

        // Màu sắc
        if (chiTietSanPham.getIdMauSac() != null && chiTietSanPham.getIdMauSac().getTenMauSac() != null) {
            tenSanPhamChiTiet.append(" - ").append(chiTietSanPham.getIdMauSac().getTenMauSac());
        }

        // Kích thước
        if (chiTietSanPham.getIdKichThuoc() != null && chiTietSanPham.getIdKichThuoc().getTenKichThuoc() != null) {
            tenSanPhamChiTiet.append(" - ").append(chiTietSanPham.getIdKichThuoc().getTenKichThuoc());
        }

        // Đế giày
        if (chiTietSanPham.getIdDeGiay() != null && chiTietSanPham.getIdDeGiay().getTenDeGiay() != null) {
            tenSanPhamChiTiet.append(" - ").append(chiTietSanPham.getIdDeGiay().getTenDeGiay());
        }

        // Chất liệu
        if (chiTietSanPham.getIdChatLieu() != null && chiTietSanPham.getIdChatLieu().getTenChatLieu() != null) {
            tenSanPhamChiTiet.append(" - ").append(chiTietSanPham.getIdChatLieu().getTenChatLieu());
        }

        return tenSanPhamChiTiet.toString();
    }

}
