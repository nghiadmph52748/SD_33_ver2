package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.be_sp.entity.NhanVien;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienResponse {
    private Integer id;
    private String maNhanVien;
    private String tenNhanVien;
    private String tenTaiKhoan;
    private String matKhau;
    private LocalDate ngaySinh;
    private String cccd;
    private String email;
    private String soDienThoai;
    private String thanhPho;
    private String quan;
    private String phuong;
    private String diaChiCuThe;
    private Boolean gioiTinh;
    private Integer idQuyenHan;
    private String tenQuyenHan;
    private String anhNhanVien;
    private Boolean trangThai;
    private Boolean deleted;

    public NhanVienResponse(NhanVien c) {
        this.id = c.getId();
        this.maNhanVien = c.getMaNhanVien();
        this.tenNhanVien = c.getTenNhanVien();
        this.tenTaiKhoan = c.getTenTaiKhoan();
        // Không trả về password trong response để bảo mật
        this.matKhau = null;
        this.ngaySinh = c.getNgaySinh();
        this.cccd = c.getCccd();
        this.email = c.getEmail();
        this.soDienThoai = c.getSoDienThoai();
        this.thanhPho = c.getThanhPho();
        this.quan = c.getQuan();
        this.phuong = c.getPhuong();
        this.diaChiCuThe = c.getDiaChiCuThe();
        this.tenQuyenHan = c.getIdQuyenHan().getTenQuyenHan();
        this.anhNhanVien = c.getAnhNhanVien();
        this.trangThai = c.getTrangThai();
        this.deleted = c.getDeleted();
        this.idQuyenHan = c.getIdQuyenHan().getId();
        this.gioiTinh = c.getGioiTinh();

    }

}
