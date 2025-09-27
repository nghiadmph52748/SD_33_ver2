package org.example.be_sp.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.be_sp.entity.ThongTinDonHang;

import java.time.LocalDate;

@Getter
@Setter
public class ThongTinDonHangResponse {
    private Integer id;
    private String maDonHang;
    private String tenTrangThaiDonHang;
    private String maThongTinDonHang;
    private LocalDate thoiGian;
    private String ghiChu;
    private Boolean deleted;

    public ThongTinDonHangResponse(ThongTinDonHang data) {
        this.id = data.getId();
        this.maDonHang = data.getIdHoaDon().getMaHoaDon();
        this.tenTrangThaiDonHang = data.getIdTrangThaiDonHang().getTenTrangThaiDonHang();
        this.maThongTinDonHang = data.getMaThongTinDonHang();
        this.thoiGian = data.getThoiGian();
        this.ghiChu = data.getGhiChu();
        this.deleted = data.getDeleted();
    }
}
