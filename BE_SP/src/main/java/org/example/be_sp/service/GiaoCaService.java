package org.example.be_sp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be_sp.entity.CaLamViec;
import org.example.be_sp.entity.GiaoCa;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.model.request.GiaoCaRequest;
import org.example.be_sp.repository.CaLamViecRepository;
import org.example.be_sp.repository.GiaoCaRepository;
import org.example.be_sp.repository.NhanVienRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GiaoCaService {

    private final GiaoCaRepository giaoCaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final CaLamViecRepository caLamViecRepository;

    // Lấy tất cả giao ca
    public List<GiaoCa> getAll() {
        return giaoCaRepository.findAll();
    }

    // Lấy giao ca theo ID
    public GiaoCa getById(Long id) {
        return giaoCaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao ca ID: " + id));
    }
    public GiaoCa create(GiaoCaRequest req) {
        // 1. Kiểm tra nhân viên & ca làm việc tồn tại
        NhanVien nguoiGiao = nhanVienRepository.findById(req.getNguoiGiaoId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên giao ca"));
        NhanVien nguoiNhan = nhanVienRepository.findById(req.getNguoiNhanId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên nhận ca"));
        CaLamViec caLamViec = caLamViecRepository.findById(req.getCaLamViecId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc"));

        // 2. Kiểm tra người giao và người nhận không trùng
        if (nguoiGiao.getId().equals(nguoiNhan.getId())) {
            throw new RuntimeException("Người giao và người nhận không thể trùng nhau");
        }

        // 3. Kiểm tra tiền
        if (req.getTongTienBanDau() != null && req.getTongTienBanDau() < 0) {
            throw new RuntimeException("Tổng tiền ban đầu không thể là số âm");
        }
        if (req.getTongTienCuoiCa() != null && req.getTongTienCuoiCa() < 0) {
            throw new RuntimeException("Tổng tiền cuối ca không thể là số âm");
        }
        if (req.getTongTienBanDau() != null && req.getTongTienCuoiCa() != null &&
                req.getTongTienCuoiCa() < req.getTongTienBanDau()) {
            throw new RuntimeException("Tổng tiền cuối ca không thể nhỏ hơn tổng tiền ban đầu");
        }

        // 4. Chuẩn hóa ngày giao ca (lấy ngày, bỏ giờ/phút/giây)
        LocalDate ngayGiaoCa = req.getThoiGianGiaoCa().toLocalDate();

        // 5. Kiểm tra trùng ca cho người nhận
        boolean daNhanCa = giaoCaRepository.existsByNguoiNhan_IdAndCaLamViec_IdAndThoiGianGiaoCaBetween(
                req.getNguoiNhanId(),
                req.getCaLamViecId(),
                ngayGiaoCa.atStartOfDay(),
                ngayGiaoCa.atTime(23, 59, 59)
        );
        if (daNhanCa) {
            throw new RuntimeException("Nhân viên này đã nhận ca này trong cùng ngày!");
        }

        // 6. Kiểm tra trùng ca cho người giao
        boolean daGiaoCa = giaoCaRepository.existsByNguoiGiao_IdAndCaLamViec_IdAndThoiGianGiaoCaBetween(
                req.getNguoiGiaoId(),
                req.getCaLamViecId(),
                ngayGiaoCa.atStartOfDay(),
                ngayGiaoCa.atTime(23, 59, 59)
        );
        if (daGiaoCa) {
            throw new RuntimeException("Nhân viên này đã giao ca này trong cùng ngày!");
        }

        // 7. Tạo giao ca
        GiaoCa giaoCa = GiaoCa.builder()
                .nguoiGiao(nguoiGiao)
                .nguoiNhan(nguoiNhan)
                .caLamViec(caLamViec)
                .thoiGianGiaoCa(req.getThoiGianGiaoCa())
                .tongTienBanDau(req.getTongTienBanDau())
                .tongTienCuoiCa(req.getTongTienCuoiCa())
                .tongTienMat(req.getTongTienMat())
                .tongTienChuyenKhoan(req.getTongTienChuyenKhoan())
                .tongTienKhac(req.getTongTienKhac())
                .tongDoanhThu(req.getTongDoanhThu())
                .tienPhatSinh(req.getTienPhatSinh())
                .tongTienThucTe(req.getTongTienThucTe())
                .chenhLech(req.getChenhlech())
                .tienMatNopLai(req.getTienMatNopLai())
                .ghiChu(req.getGhiChu())
                .build();

        return giaoCaRepository.save(giaoCa);
    }



    // Cập nhật giao ca
    public GiaoCa update(Long id, GiaoCaRequest req) {
        GiaoCa giaoCa = getById(id);

        NhanVien nguoiGiao = nhanVienRepository.findById(req.getNguoiGiaoId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên giao ca"));
        NhanVien nguoiNhan = nhanVienRepository.findById(req.getNguoiNhanId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên nhận ca"));
        CaLamViec caLamViec = caLamViecRepository.findById(req.getCaLamViecId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc"));

        giaoCa.setNguoiGiao(nguoiGiao);
        giaoCa.setNguoiNhan(nguoiNhan);
        giaoCa.setCaLamViec(caLamViec);
        giaoCa.setThoiGianGiaoCa(req.getThoiGianGiaoCa());
        giaoCa.setTongTienBanDau(req.getTongTienBanDau());
        giaoCa.setTongTienCuoiCa(req.getTongTienCuoiCa());
        giaoCa.setTongTienMat(req.getTongTienMat());
        giaoCa.setTongTienChuyenKhoan(req.getTongTienChuyenKhoan());
        giaoCa.setTongTienKhac(req.getTongTienKhac());
        giaoCa.setTongDoanhThu(req.getTongDoanhThu());
        giaoCa.setTienPhatSinh(req.getTienPhatSinh());
        giaoCa.setTongTienThucTe(req.getTongTienThucTe());
        giaoCa.setChenhLech(req.getChenhlech());
        giaoCa.setTienMatNopLai(req.getTienMatNopLai());
        giaoCa.setGhiChu(req.getGhiChu());

        return giaoCaRepository.save(giaoCa);
    }

    // Xóa giao ca
    public void delete(Long id) {
        GiaoCa giaoCa = giaoCaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao ca ID: " + id));
        giaoCaRepository.delete(giaoCa);
    }
    public GiaoCa xacNhanCa(Long giaoCaId, GiaoCaRequest req) {
        GiaoCa giaoCa = getById(giaoCaId);

        if (!giaoCa.getNguoiNhan().getId().equals(req.getNguoiNhanId())) {
            throw new RuntimeException("Người nhận ca không đúng");
        }

        // Cập nhật thông tin xác nhận
        giaoCa.setTongTienThucTe(req.getSoTienNhanThucTe());
        giaoCa.setTrangThaiXacNhan(req.getTrangThaiXacNhan());
        giaoCa.setThoiGianXacNhan(req.getThoiGianXacNhan() != null ? req.getThoiGianXacNhan() : LocalDateTime.now());
        giaoCa.setGhiChu(req.getGhiChuXacNhan());
        giaoCa.setTrangThaiCa(req.getTrangThaiCa());

        return giaoCaRepository.save(giaoCa);
    }

    @Transactional
    public Optional<GiaoCa> findPendingCaByUser(Integer nhanVienId) {
        // Giả sử "Chưa xác nhận" là trạng thái "Chờ xác nhận", bạn có thể thay đổi trạng thái này theo yêu cầu
        return giaoCaRepository.findByNguoiNhanIdAndTrangThaiXacNhan(nhanVienId, "Chưa xác nhận");
    }

}
