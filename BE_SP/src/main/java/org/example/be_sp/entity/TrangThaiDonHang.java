package org.example.be_sp.entity;

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
@Table(name = "trang_thai_don_hang")
public class TrangThaiDonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Generated(GenerationTime.ALWAYS)
    @ColumnDefault("'TTDH'+right('00000'+CONVERT([varchar](5), [ID]), 5)")
    @Column(name = "ma_trang_thai_don_hang", length = 9, insertable = false, updatable = false)
    private String maTrangThaiDonHang;

    @Nationalized
    @Column(name = "ten_trang_thai_don_hang")
    private String tenTrangThaiDonHang;

    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

    @OneToMany(mappedBy = "idTrangThaiDonHang")
    private Set<ThongTinDonHang> thongTinDonHangs = new LinkedHashSet<>();

}
