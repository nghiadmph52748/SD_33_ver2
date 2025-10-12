package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.entity.DotGiamGiaHistory;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.repository.NhanVienRepository;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DotGiamGiaHistoryResponse {
    
    private Long id;
    private Integer idDotGiamGia;
    private Integer idNhanVien;
    private String hanhDong;
    private String moTaThayDoi;
    private LocalDateTime ngayThayDoi;
    
    // Employee info
    private String tenNhanVien;
    private String maNhanVien;
    
    /**
     * Create response from entity with employee info lookup
     */
    public static DotGiamGiaHistoryResponse fromEntity(DotGiamGiaHistory entity, NhanVienRepository nhanVienRepository) {
        DotGiamGiaHistoryResponse response = new DotGiamGiaHistoryResponse();
        response.setId(entity.getId());
        response.setIdDotGiamGia(entity.getIdDotGiamGia());
        response.setIdNhanVien(entity.getIdNhanVien());
        response.setHanhDong(entity.getHanhDong());
        response.setMoTaThayDoi(entity.getMoTaThayDoi());
        response.setNgayThayDoi(entity.getNgayThayDoi());
        
        // Lookup employee info
        try {
            NhanVien nhanVien = nhanVienRepository.findById(entity.getIdNhanVien()).orElse(null);
            if (nhanVien != null) {
                response.setTenNhanVien(nhanVien.getTenNhanVien());
                response.setMaNhanVien(nhanVien.getMaNhanVien());
            }
        } catch (Exception e) {
            // Ignore if employee not found
        }
        
        return response;
    }
}
