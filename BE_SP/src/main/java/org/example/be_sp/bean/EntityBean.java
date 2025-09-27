package org.example.be_sp.bean;

import org.example.be_sp.entity.*;
import org.example.be_sp.model.request.*;
import org.example.be_sp.model.response.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EntityBean {
    @Bean
    public Class<AnhSanPham> anhSanPhamClass() {
        return AnhSanPham.class;
    }

    @Bean
    public Class<AnhSanPhamResponse> anhSanPhamResponseClass() {
        return AnhSanPhamResponse.class;
    }

    @Bean
    public Class<AnhSanPhamRequest> anhSanPhamRequestClass() {
        return AnhSanPhamRequest.class;
    }

    @Bean
    public Class<ChatLieu> chatLieuClass() {
        return ChatLieu.class;
    }

    @Bean
    public Class<ChatLieuResponse> chatLieuResponseClass() {
        return ChatLieuResponse.class;
    }

    @Bean
    public Class<ChatLieuRequest> chatLieuRequestClass() {
        return ChatLieuRequest.class;
    }

    @Bean
    public Class<MauSac> mauSacClass() {
        return MauSac.class;
    }

    @Bean
    public Class<MauSacResponse> mauSacResponseClass() {
        return MauSacResponse.class;
    }

    @Bean
    public Class<MauSacRequest> mauSacRequestClass() {
        return MauSacRequest.class;
    }

    @Bean
    public Class<KichThuoc> kichThuocClass() {
        return KichThuoc.class;
    }

    @Bean
    public Class<KichThuocRequest> kichThuocRequestClass() {
        return KichThuocRequest.class;
    }

    @Bean
    public Class<KichThuocResponse> kichThuocResponseClass() {
        return KichThuocResponse.class;
    }

    @Bean
    public Class<DeGiay> deGiayClass() {
        return DeGiay.class;
    }

    @Bean
    public Class<DeGiayRequest> deGiayRequestClass() {
        return DeGiayRequest.class;
    }

    @Bean
    public Class<DeGiayResponse> deGiayResponseClass() {
        return DeGiayResponse.class;
    }

    @Bean
    public Class<TrongLuong> trongLuongClass() {
        return TrongLuong.class;
    }

    @Bean
    public Class<TrongLuongRequest> trongLuongRequestClass() {
        return TrongLuongRequest.class;
    }

    @Bean
    public Class<TrongLuongResponse> trongLuongResponseClass() {
        return TrongLuongResponse.class;
    }

    @Bean
    public Class<XuatXu> xuatXuClass() {
        return XuatXu.class;
    }

    @Bean
    public Class<XuatXuRequest> xuatXuRequestClass() {
        return XuatXuRequest.class;
    }

    @Bean
    public Class<XuatXuResponse> xuatXuResponseClass() {
        return XuatXuResponse.class;
    }

    @Bean
    public Class<DotGiamGia> dotGiamGiaClass() {
        return DotGiamGia.class;
    }

    @Bean
    public Class<DotGiamGiaRequest> dotGiamGiaRequestClass() {
        return DotGiamGiaRequest.class;
    }

    @Bean
    public Class<DotGiamGiaResponse> dotGiamGiaResponseClass() {
        return DotGiamGiaResponse.class;
    }

    @Bean
    public Class<NhaSanXuat> nhaSanXuatClass() {
        return NhaSanXuat.class;
    }

    @Bean
    public Class<NhaSanXuatResponse> nhaSanXuatResponseClass() {
        return NhaSanXuatResponse.class;
    }

    @Bean
    public Class<NhaSanXuatRequest> nhaSanXuatRequestClass() {
        return NhaSanXuatRequest.class;
    }

    @Bean
    public Class<ChiTietSanPham> chiTietSanPhamClass() {
        return ChiTietSanPham.class;
    }

    @Bean
    public Class<ChiTietSanPhamFullResponse> chiTietSanPhamResponseClass() {
        return ChiTietSanPhamFullResponse.class;
    }

    @Bean
    public Class<ChiTietSanPhamRequest> chiTietSanPhamRequestClass() {
        return ChiTietSanPhamRequest.class;
    }

    @Bean
    public Class<QuyenHan> quyenHanClass() {
        return QuyenHan.class;
    }

    @Bean
    public Class<QuyenHanResponse> quyenHanResponseClass() {
        return QuyenHanResponse.class;
    }

    @Bean
    public Class<QuyenHanRequest> quyenHanRequestClass() {
        return QuyenHanRequest.class;
    }

    @Bean
    public Class<PhuongThucThanhToan> phuongThucThanhToanClass() {
        return PhuongThucThanhToan.class;
    }

    @Bean
    public Class<PhuongThucThanhToanResponse> phuongThucThanhToanResponseClass() {
        return PhuongThucThanhToanResponse.class;
    }

    @Bean
    public Class<PhuongThucThanhToanRequest> phuongThucThanhToanRequestClass() {
        return PhuongThucThanhToanRequest.class;
    }

    @Bean
    public Class<TrangThaiDonHang> trangThaiDonHangClass() {
        return TrangThaiDonHang.class;
    }

    @Bean
    public Class<TrangThaiDonHangResponse> trangThaiDonHangResponseClass() {
        return TrangThaiDonHangResponse.class;
    }

    @Bean
    public Class<TrangThaiDonHangRequest> trangThaiDonHangRequestClass() {
        return TrangThaiDonHangRequest.class;
    }


}
