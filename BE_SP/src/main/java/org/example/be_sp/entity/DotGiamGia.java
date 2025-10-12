package org.example.be_sp.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Nationalized;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dot_giam_gia")
public class DotGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ColumnDefault("'DGG'+right('00000'+CONVERT([varchar](5), [ID]), 5)")
    @Generated(GenerationTime.ALWAYS)
    @Column(name = "ma_dot_giam_gia", length = 8)
    private String maDotGiamGia;

    @Nationalized
    @Column(name = "ten_dot_giam_gia")
    private String tenDotGiamGia;

    @Column(name = "gia_tri_giam_gia")
    private Integer giaTriGiamGia;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "idDotGiamGia")
    private Set<ChiTietDotGiamGia> chiTietDotGiamGias = new LinkedHashSet<>();

}
