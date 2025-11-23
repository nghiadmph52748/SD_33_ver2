package org.example.be_sp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "giao_ca")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiaoCa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người giao ca
    @ManyToOne
    @JoinColumn(name = "nguoi_giao_id", nullable = false)
    private NhanVien nguoiGiao;

    // Người nhận ca
    @ManyToOne
    @JoinColumn(name = "nguoi_nhan_id", nullable = false)
    private NhanVien nguoiNhan;

    // Ca làm việc được bàn giao
    @ManyToOne
    @JoinColumn(name = "ca_lam_viec_id", nullable = false)
    private CaLamViec caLamViec;

    @Column(name = "thoi_gian_giao_ca", nullable = false)
    private LocalDateTime thoiGianGiaoCa;

    @Column(name = "tong_tien_ban_dau")
    private Double tongTienBanDau;

    @Column(name = "tong_tien_cuoi_ca")
    private Double tongTienCuoiCa;
    @Column(name = "tong_tien_mat")
    private Double tongTienMat;

    @Column(name = "tong_tien_chuyen_khoan")
    private Double tongTienChuyenKhoan;

    @Column(name = "tong_tien_khac")
    private Double tongTienKhac;

    @Column(name = "tong_doanh_thu")
    private Double tongDoanhThu;

    @Column(name = "tien_phat_sinh")
    private Double tienPhatSinh;

    @Column(name = "tong_tien_thuc_te")
    private Double tongTienThucTe;

    @Column(name = "chenh_lech")
    private Double chenhLech;

    @Column(name = "tien_mat_nop_lai")
    private Double tienMatNopLai;

    @Column(name = "so_tien_nhan_thuc_te")
    private Double soTienNhanThucTe;

    @Column(name = "trang_thai_xac_nhan")
    private String trangThaiXacNhan; // 'Chưa xác nhận', 'Đã xác nhận', 'Chênh lệch', 'Từ chối'

    /**
     * This field is optional in DB in some deployments. Marked transient to avoid
     * mapping errors when the column is missing in the database schema.
     */
    @Transient
    private LocalDateTime thoiGianXacNhan;

    @Column(name = "ghi_chu_xac_nhan")
    private String ghiChuXacNhan;

    @Column(name = "trang_thai_ca")
    private String trangThaiCa; // 'Đang làm', 'Chờ xác nhận', 'Hoàn tất'

    @Column(name = "ghi_chu")
    private String ghiChu;
}
