package org.example.be_sp.model;

import org.hibernate.annotations.Nationalized;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiaChi {
	@Nationalized
	private String tenDiaChi;
    @Nationalized
    private String diaChiCuThe;
    @Nationalized
    private String thanhPho;
    @Nationalized
    private String quan;
    @Nationalized
    private String phuong;
	private Boolean macDinh;
}
