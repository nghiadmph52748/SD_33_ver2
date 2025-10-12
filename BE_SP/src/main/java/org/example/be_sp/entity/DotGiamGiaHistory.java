package org.example.be_sp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dot_giam_gia_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DotGiamGiaHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "id_dot_giam_gia", nullable = false)
    private Integer idDotGiamGia;
    
    @Column(name = "id_nhan_vien", nullable = false)
    private Integer idNhanVien;
    
    @Column(name = "hanh_dong", length = 50, nullable = false)
    private String hanhDong;
    
    @Column(name = "mo_ta_thay_doi", columnDefinition = "nvarchar(max)")
    private String moTaThayDoi;
    
    @Column(name = "ngay_thay_doi", nullable = false)
    private LocalDateTime ngayThayDoi;
}
