package org.example.be_sp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Entity
@Table(name = "LichLamViec")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichLamViec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mỗi nhân viên có thể có nhiều lịch làm việc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id", nullable = false)
    @JsonIgnoreProperties({"hoaDons"})
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ca_lam_viec_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CaLamViec caLamViec;

    @Column(name = "ngay_lam_viec", nullable = false)
    private LocalDate ngayLamViec;


    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Boolean trangThai; // Ví dụ: "Làm việc", "Nghỉ phép", "Vắng"

    @Column(name = "ghi_chu")
    private String ghiChu;
}
