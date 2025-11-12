package org.example.be_sp.service;

import lombok.RequiredArgsConstructor;
import org.example.be_sp.entity.CaLamViec;
import org.example.be_sp.entity.GiaoCa;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.model.request.GiaoCaRequest;
import org.example.be_sp.repository.CaLamViecRepository;
import org.example.be_sp.repository.GiaoCaRepository;
import org.example.be_sp.repository.NhanVienRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    // Tạo giao ca mới (chỉ nhập tongTienBanDau)
    public GiaoCa create(GiaoCaRequest req) {
        NhanVien nguoiGiao = nhanVienRepository.findById(req.getNguoiGiaoId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên giao ca"));
        NhanVien nguoiNhan = nhanVienRepository.findById(req.getNguoiNhanId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên nhận ca"));
        CaLamViec caLamViec = caLamViecRepository.findById(req.getCaLamViecId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc"));

        GiaoCa giaoCa = GiaoCa.builder()
                .nguoiGiao(nguoiGiao)
                .nguoiNhan(nguoiNhan)
                .caLamViec(caLamViec)
                .thoiGianGiaoCa(req.getThoiGianGiaoCa())
                .tongTienBanDau(req.getTongTienBanDau())
                .tongTienCuoiCa(req.getTongTienCuoiCa())
                .ghiChu(req.getGhiChu())
                .build();

        return giaoCaRepository.save(giaoCa);
    }

    // Cập nhật giao ca (khi kết thúc ca)
    public GiaoCa update(Long id, GiaoCaRequest req) {
        GiaoCa giaoCa = getById(id);

        // Cập nhật thông tin người giao, nhận, ca
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
    // Trong GiaoCaService
    public void delete(Long id) {
        GiaoCa giaoCa = giaoCaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao ca ID: " + id));
        giaoCaRepository.delete(giaoCa);
    }


}
