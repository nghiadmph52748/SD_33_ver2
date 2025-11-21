package org.example.be_sp.entity;

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
@Table(name = "phuong_thuc_thanh_toan")
public class PhuongThucThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @ColumnDefault("'PTTT'+right('00000'+CONVERT([varchar](5), [ID]), 5)")
    @Column(name = "ma_phuong_thuc_thanh_toan", length = 9, insertable = false, updatable = false)
    private String maPhuongThucThanhToan;

    @Nationalized
    @Column(name = "ten_phuong_thuc_thanh_toan")
    private String tenPhuongThucThanhToan;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

    @OneToMany(mappedBy = "idPhuongThucThanhToan")
    private Set<HinhThucThanhToan> hinhThucThanhToans = new LinkedHashSet<>();

}
