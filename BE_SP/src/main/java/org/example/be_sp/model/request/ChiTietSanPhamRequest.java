package org.example.be_sp.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    Boolean deleted;
    LocalDate createdAt;
    Integer createBy;
    LocalDate updatedAt;
    Integer updateBy;
}
