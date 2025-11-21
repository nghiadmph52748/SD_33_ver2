package org.example.be_sp.entity;

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
@Table(name = "dia_chi_khach_hang")
public class DiaChiKhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang idKhachHang;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @ColumnDefault("'DC'+right('00000'+CONVERT([varchar](5), [ID]), 5)")
    @Column(name = "ma_dia_chi", length = 7, insertable = false, updatable = false)
    private String maDiaChi;

    @Nationalized
    @Column(name = "ten_dia_chi")
    private String tenDiaChi;

    @Nationalized
    @Column(name = "thanh_pho")
    private String thanhPho;

    @Nationalized
    @Column(name = "quan")
    private String quan;

    @Nationalized
    @Column(name = "phuong")
    private String phuong;

    @Nationalized
    @Column(name = "dia_chi_cu_the")
    private String diaChiCuThe;

    @ColumnDefault("0")
    @Column(name = "mac_dinh")
    private Boolean macDinh;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

    /**
     * Lấy địa chỉ đầy đủ của khách hàng
     */
    public String getDiaChi() {
        StringBuilder diaChi = new StringBuilder();

        if (diaChiCuThe != null && !diaChiCuThe.trim().isEmpty()) {
            diaChi.append(diaChiCuThe);
        }

        if (phuong != null && !phuong.trim().isEmpty()) {
            if (diaChi.length() > 0) {
                diaChi.append(", ");
            }
            diaChi.append(phuong);
        }

        if (quan != null && !quan.trim().isEmpty()) {
            if (diaChi.length() > 0) {
                diaChi.append(", ");
            }
            diaChi.append(quan);
        }

        if (thanhPho != null && !thanhPho.trim().isEmpty()) {
            if (diaChi.length() > 0) {
                diaChi.append(", ");
            }
            diaChi.append(thanhPho);
        }

        return diaChi.toString();
    }
}
