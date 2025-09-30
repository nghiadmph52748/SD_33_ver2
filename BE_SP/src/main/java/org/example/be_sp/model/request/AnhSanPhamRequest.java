package org.example.be_sp.model.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnhSanPhamRequest {
	String duongDanAnh;
	String tenAnh;
	Boolean trangThai;
	Boolean deleted;
	LocalDate createAt;
	Integer createBy;
	LocalDate updateAt;
	Integer updateBy;
}
