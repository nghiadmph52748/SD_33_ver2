package org.example.be_sp.model.request;

import java.time.LocalDate;

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
    Boolean trangThai;
    Integer createBy;
    Integer updateBy;
}
