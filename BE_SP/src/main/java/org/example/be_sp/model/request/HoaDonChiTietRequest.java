package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class HoaDonChiTietRequest {
    private Integer idHoaDon;

    private Integer idChiTietSanPham;

    private Integer soLuong;

    private BigDecimal giaBan;

    private BigDecimal thanhTien;

    private Boolean trangThai;

    private String ghiChu;

    private String tenSanPhamChiTiet;

    private String maSanPhamChiTiet;

    private Boolean deleted;

}
