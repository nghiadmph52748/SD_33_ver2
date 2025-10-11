package org.example.be_sp.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietSanPhamRequest {

    Integer idSanPham;
    Integer idMauSac;
    Integer idKichThuoc;
    Integer idDeGiay;
    Integer idChatLieu;
    Integer idTrongLuong;
    Integer soLuong;
    BigDecimal giaBan;
    Boolean trangThai;
    String ghiChu;
    // Tạm thời comment trường này vì cột chưa tồn tại trong database
    // String tenSanPhamChiTiet;
    @NotNull(message = "Trường deleted không được để trống")
    Boolean deleted;
    LocalDate createAt;
    Integer createBy;
    LocalDate updateAt;
    Integer updateBy;
}
