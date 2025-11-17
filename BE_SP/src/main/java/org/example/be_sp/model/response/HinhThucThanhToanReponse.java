package org.example.be_sp.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.entity.HinhThucThanhToan;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
public class HinhThucThanhToanReponse {
    private Integer id;
    private String maHoaDon;
    private String tenHoaDon;
    private Integer idPhuongThucThanhToan;
    private String tenPhuongThucThanhToan;
    private String maHinhThucThanhToan;
    private BigDecimal tienChuyenKhoan;
    private BigDecimal tienMat;
    private Boolean deleted;
    //    private String ghiChu;
    private String tenNhanVienXacNhan;
    private LocalDate ngayTao;

    public HinhThucThanhToanReponse(HinhThucThanhToan httt) {
        this.id = httt.getId();
        this.maHoaDon = httt.getIdHoaDon().getMaHoaDon();
        this.tenHoaDon = httt.getIdHoaDon().getTenHoaDon();
        this.idPhuongThucThanhToan = httt.getIdPhuongThucThanhToan().getId();
        this.tenPhuongThucThanhToan = httt.getIdPhuongThucThanhToan().getTenPhuongThucThanhToan();
        this.maHinhThucThanhToan = httt.getMaHinhThucThanhToan();
        this.tienChuyenKhoan = httt.getTienChuyenKhoan();
        this.tienMat = httt.getTienMat();
        this.deleted = httt.getDeleted();
//        this.ghiChu = httt.getGhiChu();

//        this.tenNhanVienXacNhan = httt.getNhanVienXacNhan(); // vì nó đã là String
//        if (httt.getIdHoaDon() != null) {
//            this.ngayTao = httt.getIdHoaDon().getNgayTao();
//        }
//        if (httt.getIdHoaDon() != null && httt.getIdHoaDon().getIdNhanVien() != null) {
//            this.tenNhanVienXacNhan = httt.getIdHoaDon().getIdNhanVien().getTenNhanVien();
//        }

    }
}
