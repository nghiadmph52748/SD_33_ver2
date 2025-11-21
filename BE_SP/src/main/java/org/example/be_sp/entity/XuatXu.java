package org.example.be_sp.entity;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
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
@Table(name = "xuat_xu")
public class XuatXu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ColumnDefault("'XX'+right('00000'+CONVERT([varchar](5), [ID]), 5)")
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "ma_xuat_xu", length = 7)
    private String maXuatXu;

    @Nationalized
    @Column(name = "ten_xuat_xu")
    private String tenXuatXu;

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

    @OneToMany(mappedBy = "idXuatXu")
    private Set<SanPham> sanPhams = new LinkedHashSet<>();

}
