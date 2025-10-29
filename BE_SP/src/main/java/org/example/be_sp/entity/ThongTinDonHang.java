package org.example.be_sp.entity;

import java.time.LocalDate;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Nationalized;

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
@Table(name = "thong_tin_don_hang")
public class ThongTinDonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon idHoaDon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_trang_thai_don_hang", nullable = false)
    private TrangThaiDonHang idTrangThaiDonHang;

    @Generated(GenerationTime.ALWAYS)
    @ColumnDefault("'TTDH'+right('00000'+CONVERT([varchar](5), [ID]), 5)")
    @Column(name = "ma_thong_tin_don_hang", length = 9, insertable = false, updatable = false)
    private String maThongTinDonHang;

    @Column(name = "thoi_gian")
    private LocalDate thoiGian;

    @Nationalized
    @Column(name = "ghi_chu")
    private String ghiChu;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

}
