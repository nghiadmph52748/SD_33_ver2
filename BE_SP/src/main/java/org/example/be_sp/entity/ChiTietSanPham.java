package org.example.be_sp.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Nationalized;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
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
@Table(name = "chi_tiet_san_pham")
public class ChiTietSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_san_pham", nullable = false)
    private SanPham idSanPham;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_mau_sac", nullable = false)
    private MauSac idMauSac;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_kich_thuoc", nullable = false)
    private KichThuoc idKichThuoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_de_giay", nullable = false)
    private DeGiay idDeGiay;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_chat_lieu", nullable = false)
    private ChatLieu idChatLieu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_trong_luong", nullable = false)
    private TrongLuong idTrongLuong;

    @Generated(GenerationTime.ALWAYS)
    @ColumnDefault("CONCAT('CTS', RIGHT(CONCAT('00000', CAST(ID AS VARCHAR(5))), 5))")
    @Column(name = "ma_chi_tiet_san_pham", length = 20)
    private String maChiTietSanPham;

    @ColumnDefault("0")
    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "gia_ban", precision = 18, scale = 2)
    private BigDecimal giaBan;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Nationalized
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Nationalized
    @Column(name = "ten_san_pham_chi_tiet", length = 500)
    private String tenSanPhamChiTiet;

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

    @OneToMany(mappedBy = "idChiTietSanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ChiTietDotGiamGia> chiTietDotGiamGias = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idChiTietSanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ChiTietSanPhamAnh> chiTietSanPhamAnhs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idChiTietSanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<HoaDonChiTiet> hoaDonChiTiets = new LinkedHashSet<>();
}
