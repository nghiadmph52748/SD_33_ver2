package org.example.be_sp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiaChi {
    @Nationalized
    private String diaChiCuThe;
    @Nationalized
    private String thanhPho;
    @Nationalized
    private String quan;
    @Nationalized
    private String phuong;
}
