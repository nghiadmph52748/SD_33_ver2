package org.example.be_sp.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import org.hibernate.annotations.Nationalized;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@Table(name = "quyen_han")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class QuyenHan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @ColumnDefault("'QH'+right('0'+CONVERT([varchar](1), [ID]), 1)")
    @Column(name = "ma_quyen_han", length = 3)
    private String maQuyenHan;

    @Nationalized
    @Column(name = "ten_quyen_han")
    private String tenQuyenHan;

    @ColumnDefault("0")
    @Column(name = "deleted")
    private Boolean deleted;

    @OneToMany(mappedBy = "idQuyenHan")
    @JsonIgnore
    private Set<NhanVien> nhanViens = new LinkedHashSet<>();

}
