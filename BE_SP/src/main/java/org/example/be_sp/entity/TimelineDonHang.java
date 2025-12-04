package org.example.be_sp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "lich_su_don_hang")
public class TimelineDonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don", referencedColumnName = "id")
    private HoaDon idHoaDon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_nhan_vien", referencedColumnName = "id")
    private NhanVien idNhanVien;

    @Size(max = 7)
    @Nationalized
    @ColumnDefault("'TL'+right('00000'+CONVERT([nvarchar](5), [ID]), 5)")
    @Column(name = "ma_lich_su_don_hang", length = 7, insertable = false, updatable = false)
    private String maTimeline;

    @Size(max = 100)
    @Nationalized
    @Column(name = "trang_thai_cu", length = 100)
    private String trangThaiCu;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "trang_thai_moi", nullable = false, length = 100)
    private String trangThaiMoi;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "hanh_dong", nullable = false, length = 100)
    private String hanhDong;

    @Size(max = 500)
    @Nationalized
    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Size(max = 500)
    @Nationalized
    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @NotNull
    @Column(name = "thoi_gian", nullable = false)
    private Instant thoiGian;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

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