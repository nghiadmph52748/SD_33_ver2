package org.example.be_sp.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Nationalized;

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
@Table(name = "cuoc_trao_doi")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CuocTraoDoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Generated(GenerationTime.ALWAYS)
    @ColumnDefault("'CTD'+right('00000'+CONVERT([varchar](5), [ID]), 5)")
    @Column(name = "ma_cuoc_trao_doi", length = 8, insertable = false, updatable = false)
    private String maCuocTraoDoi;

    // Staff-to-staff conversation fields (existing)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_1")
    private NhanVien nhanVien1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_2")
    private NhanVien nhanVien2;

    // Customer-to-staff conversation fields (new)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @Column(name = "loai_cuoc_trao_doi", length = 20)
    private String loaiCuocTraoDoi; // STAFF_STAFF, CUSTOMER_STAFF

    @Nationalized
    @Column(name = "tin_nhan_cuoi_cung", length = 500)
    private String tinNhanCuoiCung;

    @Column(name = "thoi_gian_tin_nhan_cuoi")
    private LocalDateTime thoiGianTinNhanCuoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nguoi_gui_cuoi")
    private NhanVien nguoiGuiCuoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang_gui_cuoi")
    private KhachHang khachHangGuiCuoi;

    @ColumnDefault("0")
    @Column(name = "so_tin_nhan_chua_doc_nv1")
    private Integer soTinNhanChuaDocNv1;

    @ColumnDefault("0")
    @Column(name = "so_tin_nhan_chua_doc_nv2")
    private Integer soTinNhanChuaDocNv2;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;

    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "create_by")
    private Integer createBy;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "update_by")
    private Integer updateBy;
}
