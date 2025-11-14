package org.example.be_sp.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.be_sp.entity.DiaChiKhachHang;
import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.DiaChi;
import org.example.be_sp.model.request.KhachHangRequest;
import org.example.be_sp.model.response.KhachHangResponse;
import org.example.be_sp.repository.DiaChiKhachHangRepository;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.util.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class KhachHangService {

    @Autowired
    private DiaChiKhachHangRepository repository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<KhachHangResponse> findAll() {
        List<KhachHang> list = khachHangRepository.findAll();
        return list.stream()
                .map(kh -> new KhachHangResponse(kh))
                .toList();
    }

    public KhachHangResponse findById(Integer id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + id));
        return new KhachHangResponse(kh);
    }

    public void save(KhachHangRequest request) {
        if (request.getEmail() != null && khachHangRepository.findByEmail(request.getEmail()) != null) {
            throw new ApiException("Email đã tồn tại: " + request.getEmail(), "404");
        } else if (request.getTenTaiKhoan() != null && khachHangRepository.existsByTenTaiKhoan(request.getTenTaiKhoan())) {
            throw new ApiException("Tên tài khoản đã tồn tại: " + request.getTenTaiKhoan(), "404");
        } else if (request.getMatKhau() == null || request.getMatKhau().isEmpty()) {
            throw new ApiException("Mật khẩu không được để trống", "404");
        }
        KhachHang khachHang = MapperUtils.map(request, KhachHang.class);
        khachHang.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        khachHang.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        KhachHang saved = khachHangRepository.save(khachHang);
        List<DiaChi> listDiaChi = request.getListDiaChi();
        if (listDiaChi != null) {
            for (DiaChi data : listDiaChi) {
                if (data == null) {
                    continue;
                }
                // Skip creating address if all fields are empty/blank as per requirement
                if (isEmptyAddress(data)) {
                    continue;
                }
                DiaChiKhachHang diaChi = new DiaChiKhachHang();
                diaChi.setThanhPho(data.getThanhPho());
                diaChi.setTenDiaChi(data.getTenDiaChi());
                diaChi.setQuan(data.getQuan());
                diaChi.setPhuong(data.getPhuong());
                diaChi.setDiaChiCuThe(data.getDiaChiCuThe());
                diaChi.setIdKhachHang(saved);
                diaChi.setDeleted(false);
                diaChi.setMacDinh(data.getMacDinh());
                repository.save(diaChi);
            }
        }
    }

    public void quickAdd(KhachHangRequest request) {
        try {
            KhachHang khachHang = MapperUtils.map(request, KhachHang.class);
            khachHang.setSoDienThoai(request.getSoDienThoai());
            khachHang.setEmail(request.getEmail());
            KhachHang saved = khachHangRepository.save(khachHang);
            List<DiaChi> listDiaChi = request.getListDiaChi();
            if (listDiaChi != null) {
                for (DiaChi data : listDiaChi) {
                    if (data == null) {
                        continue;
                    }
                    // Skip creating address if all fields are empty/blank as per requirement
                    if (isEmptyAddress(data)) {
                        continue;
                    }
                    DiaChiKhachHang diaChi = new DiaChiKhachHang();
                    diaChi.setThanhPho(data.getThanhPho());
                    diaChi.setTenDiaChi(data.getTenDiaChi());
                    diaChi.setQuan(data.getQuan());
                    diaChi.setPhuong(data.getPhuong());
                    diaChi.setDiaChiCuThe(data.getDiaChiCuThe());
                    diaChi.setIdKhachHang(saved);
                    diaChi.setDeleted(false);
                    diaChi.setMacDinh(data.getMacDinh());
                    repository.save(diaChi);
                }
            }
        } catch (Exception e) {
            if (e.getMessage().contains("could not execute statement")) {
                throw new ApiException("Số điện thoại hoặc email đã tồn tại: " + request.getSoDienThoai() + " / " + request.getEmail(), "404");
            } else {
                throw e;
            }
        }
    }

    public void update(Integer id, KhachHangRequest request) {
        // Load existing customer to handle password correctly
        KhachHang existing = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + id));

        KhachHang khachHang = MapperUtils.map(request, KhachHang.class);
        khachHang.setId(id);

        // Password handling: keep existing if null/blank; if provided, only re-encode when it doesn't match
        String reqPwd = request.getMatKhau();
        String currentHash = existing.getMatKhau();
        if (reqPwd == null || reqPwd.isEmpty()) {
            khachHang.setMatKhau(currentHash);
        } else if (passwordEncoder.matches(reqPwd, currentHash)) {
            khachHang.setMatKhau(currentHash);
        } else {
            khachHang.setMatKhau(passwordEncoder.encode(reqPwd));
        }

        KhachHang saved = khachHangRepository.save(khachHang);

        // Address handling: add new addresses that don't already exist
        List<DiaChi> listDiaChi = request.getListDiaChi();
        if (listDiaChi != null) {
            List<DiaChiKhachHang> existingAddrs = repository.findAllByIdKhachHang_Id(id);
            for (DiaChi data : listDiaChi) {
                // Skip null or empty address objects to avoid creating invalid records
                if (data == null || isEmptyAddress(data)) {
                    continue;
                }
                boolean exists = existingAddrs.stream().anyMatch(dckh
                        -> Objects.equals(dckh.getDiaChiCuThe(), data.getDiaChiCuThe())
                        && Objects.equals(dckh.getThanhPho(), data.getThanhPho())
                        && Objects.equals(dckh.getQuan(), data.getQuan())
                        && Objects.equals(dckh.getPhuong(), data.getPhuong())
                );
                if (!exists) {
                    DiaChiKhachHang diaChi = new DiaChiKhachHang();
                    diaChi.setThanhPho(data.getThanhPho());
                    diaChi.setTenDiaChi(data.getTenDiaChi());
                    diaChi.setQuan(data.getQuan());
                    diaChi.setPhuong(data.getPhuong());
                    diaChi.setDiaChiCuThe(data.getDiaChiCuThe());
                    diaChi.setIdKhachHang(saved);
                    diaChi.setDeleted(false);
                    diaChi.setMacDinh(data.getMacDinh());
                    repository.save(diaChi);
                }
            }
        }
    }

    public void updateStatus(Integer id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + id));
        // Giả sử trangThai là trạng thái active/inactive
        kh.setTrangThai(!kh.getTrangThai());
        khachHangRepository.save(kh);
    }

    // Helpers: determine whether address data is empty (all fields blank or null)
    private boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isEmptyAddress(DiaChi d) {
        return d == null
                || (isNullOrBlank(d.getThanhPho())
                && isNullOrBlank(d.getTenDiaChi())
                && isNullOrBlank(d.getQuan())
                && isNullOrBlank(d.getPhuong())
                && isNullOrBlank(d.getDiaChiCuThe()));
    }

    public ByteArrayInputStream exportKhachHangToExcel() throws IOException {
        String[] columns = {"ID", "Mã KH", "Tên KH", "Email", "SĐT", "Giới tính"};

        List<KhachHang> khachHangs = khachHangRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("KhachHang");

            // Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Data
            int rowIdx = 1;
            for (KhachHang kh : khachHangs) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(kh.getId());
                row.createCell(1).setCellValue(kh.getMaKhachHang());
                row.createCell(2).setCellValue(kh.getTenKhachHang());
                row.createCell(3).setCellValue(kh.getEmail());
                row.createCell(4).setCellValue(kh.getSoDienThoai());
                row.createCell(5).setCellValue(kh.getGioiTinh() != null && kh.getGioiTinh() ? "Nam" : "Nữ");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
