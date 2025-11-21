package org.example.be_sp.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import org.hibernate.annotations.Nationalized;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hoa_don_chi_tiet")
public class HoaDonChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    @JsonBackReference
    private HoaDon idHoaDon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_chi_tiet_san_pham", nullable = false)
    private ChiTietSanPham idChiTietSanPham;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @ColumnDefault("'HDCT'+right('00000'+CONVERT([varchar](5), [ID]), 5)")
    @Column(name = "ma_hoa_don_chi_tiet", length = 9,insertable = false, updatable = false)
    private String maHoaDonChiTiet;

    @ColumnDefault("0")
    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "gia_ban", precision = 18, scale = 2)
    private BigDecimal giaBan;

    @Column(name = "thanh_tien", precision = 18, scale = 2)
    private BigDecimal thanhTien;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Nationalized
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Nationalized
    @Column(name = "ten_san_pham_chi_tiet")
    private String tenSanPhamChiTiet;

    @Column(name = "ma_san_pham_chi_tiet", length = 20)
    private String maSanPhamChiTiet;

    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "create_at")
    private LocalDate createAt;

    @Column(name = "create_by")
    private Integer createBy;

    @Column(name = "update_at")
    private LocalDate updateAt;

    @Column(name = "update_by")
    private Integer updateBy;
}
