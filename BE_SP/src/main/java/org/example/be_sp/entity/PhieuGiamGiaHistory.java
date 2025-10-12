package org.example.be_sp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "phieu_giam_gia_history")
public class PhieuGiamGiaHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_phieu_giam_gia", nullable = false)
    private Integer idPhieuGiamGia;

    @Column(name = "id_nhan_vien", nullable = false)
    private Integer idNhanVien;

    @Column(name = "hanh_dong", nullable = false, length = 50)
    private String hanhDong;

    @Column(name = "mo_ta_thay_doi", columnDefinition = "NVARCHAR(MAX)")
    private String moTaThayDoi;

    @Column(name = "ngay_thay_doi", nullable = false)
    private LocalDateTime ngayThayDoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_giam_gia", insertable = false, updatable = false)
    private PhieuGiamGia phieuGiamGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien", insertable = false, updatable = false)
    private NhanVien nhanVien;

    @PrePersist
    protected void onCreate() {
        if (ngayThayDoi == null) {
            ngayThayDoi = LocalDateTime.now();
        }
    }
}
