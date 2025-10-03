package org.example.be_sp.model.request;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnhSanPhamUploadCloud {
	MultipartFile[] duongDanAnh;
	String tenAnh;
	String mauAnh;
	Boolean trangThai;
	Integer createBy;
	Integer updateBy;
}
