package org.example.be_sp.model.response;

import java.math.BigDecimal;

/**
 * Response when adding product to cart
 */
public class AddProductToCartResponse {

    private Integer idHoaDon;               // Cart invoice ID
    private Integer idChiTietHoaDon;        // Invoice detail ID
    private Integer soLuong;                // Quantity
    private BigDecimal giaBan;              // Unit price
    private BigDecimal thanhTien;           // Total amount (quantity * price)

    public AddProductToCartResponse() {
    }

    public AddProductToCartResponse(Integer idHoaDon, Integer idChiTietHoaDon, Integer soLuong,
            BigDecimal giaBan, BigDecimal thanhTien) {
        this.idHoaDon = idHoaDon;
        this.idChiTietHoaDon = idChiTietHoaDon;
        this.soLuong = soLuong;
        this.giaBan = giaBan;
        this.thanhTien = thanhTien;
    }

    public Integer getIdHoaDon() {
        return idHoaDon;
    }

    public void setIdHoaDon(Integer idHoaDon) {
        this.idHoaDon = idHoaDon;
    }

    public Integer getIdChiTietHoaDon() {
        return idChiTietHoaDon;
    }

    public void setIdChiTietHoaDon(Integer idChiTietHoaDon) {
        this.idChiTietHoaDon = idChiTietHoaDon;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }
}
