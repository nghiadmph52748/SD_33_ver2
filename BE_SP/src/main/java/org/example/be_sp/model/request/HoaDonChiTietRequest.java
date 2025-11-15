package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class HoaDonChiTietRequest {
    private Integer idHoaDon;

    // Backend expects idChiTietSanPham, but FE may send idBienThe or idBienTheSanPham
    // Support all to avoid breaking changes in FE
    private Integer idChiTietSanPham;      // preferred backend name
    private Integer idBienTheSanPham;      // alias from FE
    private Integer idBienThe;             // from banHangMain (OrderItem.idBienThe)

    private Integer soLuong;

    private BigDecimal giaBan;

    private BigDecimal thanhTien;

    private Boolean trangThai;

    private String ghiChu;

    private String tenSanPhamChiTiet;

    private String maSanPhamChiTiet;

    private Boolean deleted;

    private LocalDate createAt;

    private Integer createBy;

    private LocalDate updateAt;

    private Integer updateBy;
}
