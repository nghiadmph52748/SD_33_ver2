package org.example.be_sp.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ma_phieu_giam_gia", length = 50, nullable = false, unique = true)
    private String maPhieuGiamGia;

    @Nationalized
    @Column(name = "ten_phieu_giam_gia")
    private String tenPhieuGiamGia;

    @ColumnDefault("0")
    @Column(name = "loai_phieu_giam_gia")
    private Boolean loaiPhieuGiamGia;

    @Column(name = "gia_tri_giam_gia", precision = 18, scale = 2)
    private BigDecimal giaTriGiamGia;

    @Column(name = "hoa_don_toi_thieu", precision = 18, scale = 2)
    private BigDecimal hoaDonToiThieu;

    @Column(name = "so_luong_dung")
    private Integer soLuongDung;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Nationalized
    @Column(name = "mo_ta")
    private String moTa;

    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

    @ColumnDefault("0")
    @Column(name = "noi_bat")
    private Boolean featured;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "idPhieuGiamGia")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idPhieuGiamGia")
    private Set<PhieuGiamGiaCaNhan> phieuGiamGiaCaNhans = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idPhieuGiamGia")
    private Set<ChiTietPhieuGiamGia> chiTietPhieuGiamGias = new LinkedHashSet<>();

    /**
     * Set default values before persisting
     */
    @PrePersist
    public void prePersist() {
        if (this.deleted == null) {
            this.deleted = false;
        }
        if (this.trangThai == null) {
            this.trangThai = true;
        }
        if (this.featured == null) {
            this.featured = false;
        }
        if (this.loaiPhieuGiamGia == null) {
            this.loaiPhieuGiamGia = false;
        }
    }
}
