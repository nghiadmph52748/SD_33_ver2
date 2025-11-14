package org.example.be_sp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalTime;

@Entity
@Table(name = "ca_lam_viec")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaLamViec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_ca", nullable = false)
    private String tenCa;

    @Column(name = "thoi_gian_bat_dau", nullable = false)
    private LocalTime thoiGianBatDau;

    @Column(name = "thoi_gian_ket_thuc", nullable = false)
    private LocalTime thoiGianKetThuc;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai;
}
