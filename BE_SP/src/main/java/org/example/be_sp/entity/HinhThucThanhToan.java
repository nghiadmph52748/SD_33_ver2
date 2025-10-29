package org.example.be_sp.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@Table(name = "hinh_thuc_thanh_toan")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HinhThucThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    @JsonBackReference
    private HoaDon idHoaDon;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_phuong_thuc_thanh_toan", nullable = false)
    private PhuongThucThanhToan idPhuongThucThanhToan;

    @Generated(GenerationTime.ALWAYS)
    @ColumnDefault("'HTTT'+right('00000'+CONVERT([varchar](5), [ID]), 5)")
    @Column(name = "ma_hinh_thuc_thanh_toan", length = 9, insertable = false, updatable = false)
    private String maHinhThucThanhToan;

    @Column(name = "tien_chuyen_khoan", precision = 18, scale = 2)
    private BigDecimal tienChuyenKhoan;

    @Column(name = "tien_mat", precision = 18, scale = 2)
    private BigDecimal tienMat;

//    @Column(name = "ngay_tao")
//    private LocalDateTime ngayTao;

//    @Column(name = "loai_giao_dich")
//    private String loaiGiaoDich;

    @Column(name = "trang_thai")
    private String trangThai;


//    @Column(name = "nhan_vien_xac_nhan")
//    private String nhanVienXacNhan;


    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

}
