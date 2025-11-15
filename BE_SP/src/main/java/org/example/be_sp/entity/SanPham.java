package org.example.be_sp.entity;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Nationalized;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "san_pham")
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_nha_san_xuat", nullable = false)
    private NhaSanXuat idNhaSanXuat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_xuat_xu", nullable = false)
    private XuatXu idXuatXu;

    @Generated(GenerationTime.ALWAYS)
    @ColumnDefault("'SP'+right('00000'+CONVERT([varchar](5), [ID]), 5)")
    @Column(name = "ma_san_pham", length = 7, updatable = false, insertable = false)
    private String maSanPham;

    @Nationalized
    @Column(name = "ten_san_pham")
    private String tenSanPham;

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

    @OneToMany(mappedBy = "idSanPham", fetch = FetchType.EAGER)
    private Set<ChiTietSanPham> chiTietSanPhams = new LinkedHashSet<>();

}
