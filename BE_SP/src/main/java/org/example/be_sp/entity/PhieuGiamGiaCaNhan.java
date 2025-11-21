package org.example.be_sp.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
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
@Table(name = "phieu_giam_gia_ca_nhan")
public class PhieuGiamGiaCaNhan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang idKhachHang;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_phieu_giam_gia", nullable = false)
    private PhieuGiamGia idPhieuGiamGia;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @ColumnDefault("'PGGCN'+right('00000'+CONVERT([varchar](5), [ID]), 5)")
    @Column(name = "ma_phieu_giam_gia_ca_nhan", length = 10)
    private String maPhieuGiamGiaCaNhan;

    @Nationalized
    @Column(name = "ten_phieu_giam_gia_ca_nhan")
    private String tenPhieuGiamGiaCaNhan;

    @Column(name = "ngay_nhan")
    private LocalDateTime ngayNhan;

    @Column(name = "ngay_het_han")
    private LocalDateTime ngayHetHan;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

}
