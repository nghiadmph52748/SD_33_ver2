package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.entity.PhieuGiamGiaHistory;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuGiamGiaHistoryResponse {

    private Long id;
    private Integer idPhieuGiamGia;
    private Integer idNhanVien;
    private String tenNhanVien;
    private String maNhanVien;
    private String hanhDong;
    private String moTaThayDoi;
    private LocalDateTime ngayThayDoi;

    /**
     * Map from entity to response DTO
     * @param history The history entity
     * @return Response DTO
     */
    public static PhieuGiamGiaHistoryResponse fromEntity(PhieuGiamGiaHistory history) {
        return fromEntity(history, null);
    }

    /**
     * Map from entity to response DTO with employee information
     * @param history The history entity
     * @param nhanVien The employee entity (can be null)
     * @return Response DTO
     */
    public static PhieuGiamGiaHistoryResponse fromEntity(PhieuGiamGiaHistory history, NhanVien nhanVien) {
        PhieuGiamGiaHistoryResponseBuilder builder = PhieuGiamGiaHistoryResponse.builder()
                .id(history.getId())
                .idPhieuGiamGia(history.getIdPhieuGiamGia())
                .idNhanVien(history.getIdNhanVien())
                .hanhDong(history.getHanhDong())
                .moTaThayDoi(history.getMoTaThayDoi())
                .ngayThayDoi(history.getNgayThayDoi());

        // Add employee info if available
        if (nhanVien != null) {
            builder.tenNhanVien(nhanVien.getTenNhanVien())
                   .maNhanVien(nhanVien.getMaNhanVien());
        } else if (history.getNhanVien() != null) {
            builder.tenNhanVien(history.getNhanVien().getTenNhanVien())
                   .maNhanVien(history.getNhanVien().getMaNhanVien());
        }

        return builder.build();
    }
}
