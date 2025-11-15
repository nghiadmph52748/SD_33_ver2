package org.example.be_sp.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.be_sp.entity.SanPham;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SanPhamResponse {

    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private Integer soLuongBienThe;
    private BigDecimal giaNhoNhat;
    private BigDecimal giaLonNhat;
    private Boolean trangThai;
    private Boolean deleted;
    private Integer idNhaSanXuat;
    private String tenNhaSanXuat;
    private Integer idXuatXu;
    private LocalDate createAt;
    private Integer createBy;
    private LocalDate updateAt;
    private Integer updateBy;
    private List<BienTheResponse> bienThe;
    private List<HinhAnhResponse> hinhAnh;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BienTheResponse {

        private Integer id;
        private Integer idSanPham;
        private Integer idKichThuoc;
        private String tenKichThuoc;
        private Integer idMauSac;
        private String tenMauSac;
        private Integer soLuong;
        private BigDecimal giaBan;
        private BigDecimal giaTriGiamGia;
        private Integer idDotGiamGia;
        private String tenDotGiamGia;
        private Boolean trangThai;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class HinhAnhResponse {

        private Integer id;
        private Integer idSanPham;
        private String urlAnh;
        private Boolean trangThai;
    }

    public SanPhamResponse(SanPham sp) {
        this.id = sp.getId();
        this.maSanPham = sp.getMaSanPham();
        this.tenSanPham = sp.getTenSanPham();
        this.soLuongBienThe = sp.getChiTietSanPhams().size();

        // Calculate min and max prices from variants
        List<BigDecimal> giaBan = sp.getChiTietSanPhams().stream()
                .filter(ct -> ct.getGiaBan() != null)
                .map(ct -> ct.getGiaBan())
                .toList();
        this.giaNhoNhat = giaBan.stream().min(BigDecimal::compareTo).orElse(null);
        this.giaLonNhat = giaBan.stream().max(BigDecimal::compareTo).orElse(null);

        this.trangThai = sp.getTrangThai();
        this.deleted = sp.getDeleted();
        this.idNhaSanXuat = sp.getIdNhaSanXuat().getId();
        this.tenNhaSanXuat = sp.getIdNhaSanXuat().getTenNhaSanXuat();
        this.idXuatXu = sp.getIdXuatXu().getId();
        this.createAt = sp.getCreateAt();
        this.createBy = sp.getCreateBy();
        this.updateAt = sp.getUpdateAt();
        this.updateBy = sp.getUpdateBy();

        // Build variant responses with discount info
        this.bienThe = sp.getChiTietSanPhams().stream()
                .map(ct -> {
                    BienTheResponse bienTheResp = new BienTheResponse();
                    bienTheResp.setId(ct.getId());
                    bienTheResp.setIdSanPham(ct.getIdSanPham().getId());
                    bienTheResp.setIdKichThuoc(ct.getIdKichThuoc().getId());
                    bienTheResp.setTenKichThuoc(ct.getIdKichThuoc().getTenKichThuoc());
                    bienTheResp.setIdMauSac(ct.getIdMauSac().getId());
                    bienTheResp.setTenMauSac(ct.getIdMauSac().getTenMauSac());
                    bienTheResp.setSoLuong(ct.getSoLuong());
                    bienTheResp.setGiaBan(ct.getGiaBan());

                    // Get discount info from active discount promotion
                    // Check: trangThai = true, not deleted, and within valid date range
                    ct.getChiTietDotGiamGias().stream()
                            .filter(cdgg -> cdgg.getTrangThai() != null && cdgg.getTrangThai()
                            && (cdgg.getDeleted() == null || !cdgg.getDeleted())
                            && cdgg.getIdDotGiamGia() != null
                            && cdgg.getIdDotGiamGia().getTrangThai() != null
                            && cdgg.getIdDotGiamGia().getTrangThai()
                            && isDiscountActive(cdgg.getIdDotGiamGia().getNgayBatDau(),
                                    cdgg.getIdDotGiamGia().getNgayKetThuc()))
                            .findFirst()
                            .ifPresent(cdgg -> {
                                bienTheResp.setGiaTriGiamGia(BigDecimal.valueOf(cdgg.getIdDotGiamGia().getGiaTriGiamGia()));
                                bienTheResp.setIdDotGiamGia(cdgg.getIdDotGiamGia().getId());
                                bienTheResp.setTenDotGiamGia(cdgg.getIdDotGiamGia().getTenDotGiamGia());
                            });

                    bienTheResp.setTrangThai(ct.getTrangThai());
                    return bienTheResp;
                })
                .toList();

        // Build image responses from variants
        this.hinhAnh = sp.getChiTietSanPhams().stream()
                .flatMap(ct -> ct.getChiTietSanPhamAnhs().stream())
                .map(ctAnh -> {
                    HinhAnhResponse hinhAnhResp = new HinhAnhResponse();
                    hinhAnhResp.setId(ctAnh.getIdAnhSanPham().getId());
                    hinhAnhResp.setIdSanPham(sp.getId());
                    hinhAnhResp.setUrlAnh(ctAnh.getIdAnhSanPham().getDuongDanAnh());
                    hinhAnhResp.setTrangThai(ctAnh.getTrangThai());
                    return hinhAnhResp;
                })
                .distinct()
                .toList();
    }

    // Check if discount is within valid date range (today is between ngayBatDau and ngayKetThuc)
    private boolean isDiscountActive(LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
        if (ngayBatDau == null || ngayKetThuc == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(ngayBatDau) && !now.isAfter(ngayKetThuc);
    }
}
