package org.example.be_sp.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hoa_don")
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang idKhachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_giam_gia")
    private PhieuGiamGia idPhieuGiamGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien idNhanVien;

    @Column(name = "ma_hoa_don", length = 10)
    private String maHoaDon;

    @Nationalized
    @Column(name = "ten_hoa_don")
    private String tenHoaDon;

    @ColumnDefault("0")
    @Column(name = "loai_don")
    private Boolean giaoHang;

    @Column(name = "phi_van_chuyen", precision = 18, scale = 2)
    private BigDecimal phiVanChuyen;

    @Column(name = "tong_tien", precision = 18, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "tong_tien_sau_giam", precision = 18, scale = 2)
    private BigDecimal tongTienSauGiam;

    @Nationalized
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Nationalized
    @Column(name = "ten_khach_hang")
    private String tenNguoiNhan;

    @Nationalized
    @Column(name = "dia_chi_khach_hang")
    private String diaChiNguoiNhan;

    @Column(name = "so_dien_thoai_khach_hang", length = 12, columnDefinition = "nvarchar(12)")
    private String soDienThoaiNguoiNhan;

    @Column(name = "email_khach_hang", columnDefinition = "nvarchar(255)")
    private String emailNguoiNhan;

    @Nationalized
    @Column(name = "ten_nhan_vien")
    private String tenNhanVien;

    @Column(name = "ma_nhan_vien", length = 7)
    private String maNhanVien;

    @Nationalized
    @Column(name = "ten_phieu_giam_gia")
    private String tenPhieuGiamGia;

    @Column(name = "ma_phieu_giam_gia", length = 7)
    private String maPhieuGiamGia;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "create_by")
    private Integer createBy;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "update_by")
    private Integer updateBy;

    @Column(name = "trang_thai_thanh_toan")
    private Boolean trangThaiThanhToan;

    @Column(name = "so_tien_da_thanh_toan", precision = 18, scale = 2)
    private BigDecimal soTienDaThanhToan;

    @Column(name = "so_tien_con_lai", precision = 18, scale = 2)
    private BigDecimal soTienConLai;

    @Column(name = "phu_phi", precision = 18, scale = 2)
    private BigDecimal phuPhi;

    @Column(name = "hoan_phi", precision = 18, scale = 2)
    private BigDecimal hoanPhi;

    @OneToMany(mappedBy = "idHoaDon")
    @JsonManagedReference
    private Set<HinhThucThanhToan> hinhThucThanhToans;

    @OneToMany(mappedBy = "idHoaDon")
    private Set<HoaDonChiTiet> hoaDonChiTiets = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idHoaDon")
    private Set<ThongTinDonHang> thongTinDonHangs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idHoaDon")
    private Set<TimelineDonHang> timelineDonHangs = new LinkedHashSet<>();

}
