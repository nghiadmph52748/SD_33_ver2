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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tin_nhan")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TinNhan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Generated(GenerationTime.ALWAYS)
    @ColumnDefault("'TN'+right('00000'+CONVERT([varchar](5), [ID]), 5)")
    @Column(name = "ma_tin_nhan", length = 7, insertable = false, updatable = false)
    private String maTinNhan;

    @NotNull(message = "Người gửi không được để trống")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_nguoi_gui", nullable = false)
    private NhanVien nguoiGui;

    @NotNull(message = "Người nhận không được để trống")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_nguoi_nhan", nullable = false)
    private NhanVien nguoiNhan;

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    @Nationalized
    @Column(name = "noi_dung", columnDefinition = "nvarchar(max)", nullable = false)
    private String noiDung;

    @ColumnDefault("'TEXT'")
    @Column(name = "loai_tin_nhan", length = 20)
    private String loaiTinNhan; // TEXT, IMAGE, FILE

    @ColumnDefault("0")
    @Column(name = "da_doc")
    private Boolean daDoc;

    @ColumnDefault("GETDATE()")
    @Column(name = "thoi_gian_gui")
    private LocalDateTime thoiGianGui;

    @Column(name = "thoi_gian_doc")
    private LocalDateTime thoiGianDoc;

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
