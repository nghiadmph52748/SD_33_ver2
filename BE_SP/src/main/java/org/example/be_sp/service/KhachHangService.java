package org.example.be_sp.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.be_sp.entity.DiaChiKhachHang;
import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.DiaChi;
import org.example.be_sp.model.request.CustomerSegmentFilter;
import org.example.be_sp.model.request.KhachHangRequest;
import org.example.be_sp.model.response.CustomerSegmentResponse;
import org.example.be_sp.model.response.KhachHangResponse;
import org.example.be_sp.model.response.PagedResponse;
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
    @PersistenceContext
    private EntityManager entityManager;

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int HIGH_FREQUENCY_MIN_ORDERS = 3;
    private static final int RFM_HIGH_FREQUENCY = 5;
    private static final int RFM_MEDIUM_FREQUENCY = 3;
    private static final long RFM_HIGH_MONETARY = 10_000_000L;
    private static final long RFM_MEDIUM_MONETARY = 5_000_000L;

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

    @Transactional
    public PagedResponse<CustomerSegmentResponse> getCustomersBySegment(CustomerSegmentFilter filter) {
        int page = filter.getPage() != null && filter.getPage() > 0 ? filter.getPage() : DEFAULT_PAGE;
        int size = filter.getPageSize() != null && filter.getPageSize() > 0 ? filter.getPageSize() : DEFAULT_PAGE_SIZE;
        int offset = (page - 1) * size;

        StringBuilder baseSql = new StringBuilder(" FROM vw_khach_hang_rfm WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<>();

        applySegmentConditions(baseSql, params, filter);
        applyCommonFilters(baseSql, params, filter);

        String selectSql = "SELECT id, ma_khach_hang, ten_khach_hang, email, so_dien_thoai, ngay_sinh, phan_loai,"
                + " orders_count, first_order_at, last_order_at, total_spent, days_since_last_order "
                + baseSql
                + " ORDER BY COALESCE(last_order_at, first_order_at, CAST('1900-01-01' AS DATETIME)) DESC, ten_khach_hang ASC";

        Query query = entityManager.createNativeQuery(selectSql);
        params.forEach(query::setParameter);
        query.setFirstResult(offset);
        query.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = (List<Object[]>) query.getResultList();
        List<CustomerSegmentResponse> content = mapToCustomerResponses(rows);

        String countSql = "SELECT COUNT(1) " + baseSql;
        Query countQuery = entityManager.createNativeQuery(countSql);
        params.forEach(countQuery::setParameter);
        Number totalElements = (Number) countQuery.getSingleResult();
        long total = totalElements != null ? totalElements.longValue() : 0L;
        long totalPages = size == 0 ? 0 : (long) Math.ceil((double) total / size);

        return PagedResponse.<CustomerSegmentResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    private void applySegmentConditions(StringBuilder sql, Map<String, Object> params, CustomerSegmentFilter filter) {
        String type = filter.getSegmentType() != null ? filter.getSegmentType().toLowerCase() : "all";
        String key = filter.getSegmentKey() != null ? filter.getSegmentKey().toLowerCase() : null;

        switch (type) {
            case "behavior" -> applyBehaviorSegment(sql, params, key);
            case "rfm" -> applyRfmSegment(sql, params, key);
            case "event" -> applyEventSegment(sql, params, key, filter);
            default -> {
                // default all customers - no extra condition
            }
        }
    }

    private void applyBehaviorSegment(StringBuilder sql, Map<String, Object> params, String key) {
        if (key == null) {
            return;
        }

        switch (key) {
            case "new_customers" ->
                    sql.append(" AND COALESCE(orders_count, 0) = 0");
            case "first_order" ->
                    sql.append(" AND COALESCE(orders_count, 0) = 1");
            case "repeat_customers" ->
                    sql.append(" AND COALESCE(orders_count, 0) >= 2");
            case "high_frequency" -> {
                sql.append(" AND COALESCE(orders_count, 0) >= :hfMinOrders")
                        .append(" AND last_order_at IS NOT NULL")
                        .append(" AND last_order_at >= DATEADD(DAY, -30, GETDATE())");
                params.put("hfMinOrders", HIGH_FREQUENCY_MIN_ORDERS);
            }
            case "inactive_30" ->
                    sql.append(" AND (last_order_at IS NULL OR last_order_at < DATEADD(DAY, -30, GETDATE()))");
            case "inactive_60" ->
                    sql.append(" AND (last_order_at IS NULL OR last_order_at < DATEADD(DAY, -60, GETDATE()))");
            case "inactive_90" ->
                    sql.append(" AND (last_order_at IS NULL OR last_order_at < DATEADD(DAY, -90, GETDATE()))");
            default -> {
            }
        }
    }

    private void applyRfmSegment(StringBuilder sql, Map<String, Object> params, String key) {
        if (key == null) {
            return;
        }
        switch (key) {
            case "loyal_high_value" -> {
                sql.append(" AND COALESCE(orders_count, 0) >= :rfmHighFrequency")
                        .append(" AND COALESCE(total_spent, 0) >= :rfmHighMonetary")
                        .append(" AND COALESCE(days_since_last_order, 9999) <= 30");
                params.put("rfmHighFrequency", RFM_HIGH_FREQUENCY);
                params.put("rfmHighMonetary", RFM_HIGH_MONETARY);
            }
            case "potential_loyal" -> {
                sql.append(" AND COALESCE(orders_count, 0) BETWEEN :rfmMediumFreq AND :rfmHighFreqMinusOne")
                        .append(" AND COALESCE(total_spent, 0) BETWEEN :rfmMediumMoney AND :rfmHighMoney")
                        .append(" AND COALESCE(days_since_last_order, 9999) <= 45");
                params.put("rfmMediumFreq", RFM_MEDIUM_FREQUENCY);
                params.put("rfmHighFreqMinusOne", RFM_HIGH_FREQUENCY - 1);
                params.put("rfmMediumMoney", RFM_MEDIUM_MONETARY);
                params.put("rfmHighMoney", RFM_HIGH_MONETARY);
            }
            case "big_spenders" -> {
                sql.append(" AND COALESCE(total_spent, 0) >= :rfmBigSpender");
                params.put("rfmBigSpender", RFM_HIGH_MONETARY);
            }
            case "at_risk" -> {
                sql.append(" AND COALESCE(days_since_last_order, 9999) BETWEEN 45 AND 90")
                        .append(" AND (COALESCE(orders_count, 0) >= :rfmHighFreq OR COALESCE(total_spent, 0) >= :rfmHighMoney)");
                params.put("rfmHighFreq", RFM_HIGH_FREQUENCY);
                params.put("rfmHighMoney", RFM_HIGH_MONETARY);
            }
            case "lost" ->
                    sql.append(" AND COALESCE(days_since_last_order, 9999) > 90 AND COALESCE(orders_count, 0) > 0");
            default -> {
            }
        }
    }

    private void applyEventSegment(StringBuilder sql, Map<String, Object> params, String key, CustomerSegmentFilter filter) {
        if (key == null) {
            return;
        }
        switch (key) {
            case "birthday_month" ->
                    sql.append(" AND ngay_sinh IS NOT NULL AND MONTH(ngay_sinh) = MONTH(GETDATE())");
            case "birthday_upcoming" -> {
                int days = filter.getBirthdayDays() != null && filter.getBirthdayDays() > 0 ? filter.getBirthdayDays() : 7;
                sql.append(" AND ngay_sinh IS NOT NULL")
                        .append(" AND DATEDIFF(DAY, CAST(GETDATE() AS DATE), ")
                        .append("DATEFROMPARTS(")
                        .append("YEAR(GETDATE()) + CASE WHEN DATEFROMPARTS(YEAR(GETDATE()), MONTH(ngay_sinh), DAY(ngay_sinh)) < CAST(GETDATE() AS DATE) THEN 1 ELSE 0 END, ")
                        .append("MONTH(ngay_sinh), DAY(ngay_sinh))) BETWEEN 0 AND :birthdayDays");
                params.put("birthdayDays", days);
            }
            default -> {
            }
        }
    }

    private void applyCommonFilters(StringBuilder sql, Map<String, Object> params, CustomerSegmentFilter filter) {
        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            String value = filter.getSearch().trim().toLowerCase();
            sql.append(" AND (LOWER(ten_khach_hang) LIKE :searchText OR LOWER(email) LIKE :searchText OR so_dien_thoai LIKE :searchPhone)");
            params.put("searchText", "%" + value + "%");
            params.put("searchPhone", "%" + filter.getSearch().trim() + "%");
        }

        if (filter.getMinOrdersCount() != null) {
            sql.append(" AND COALESCE(orders_count, 0) >= :minOrders");
            params.put("minOrders", filter.getMinOrdersCount());
        }
        if (filter.getMaxOrdersCount() != null) {
            sql.append(" AND COALESCE(orders_count, 0) <= :maxOrders");
            params.put("maxOrders", filter.getMaxOrdersCount());
        }
        if (filter.getMinTotalSpent() != null) {
            sql.append(" AND COALESCE(total_spent, 0) >= :minTotalSpent");
            params.put("minTotalSpent", filter.getMinTotalSpent());
        }
        if (filter.getMaxTotalSpent() != null) {
            sql.append(" AND COALESCE(total_spent, 0) <= :maxTotalSpent");
            params.put("maxTotalSpent", filter.getMaxTotalSpent());
        }
        if (filter.getLastOrderFrom() != null) {
            sql.append(" AND last_order_at IS NOT NULL AND last_order_at >= :lastOrderFrom");
            params.put("lastOrderFrom", Timestamp.valueOf(filter.getLastOrderFrom().atStartOfDay()));
        }
        if (filter.getLastOrderTo() != null) {
            LocalDateTime exclusiveEnd = filter.getLastOrderTo().plusDays(1).atStartOfDay();
            sql.append(" AND last_order_at IS NOT NULL AND last_order_at < :lastOrderTo");
            params.put("lastOrderTo", Timestamp.valueOf(exclusiveEnd));
        }
    }

    private List<CustomerSegmentResponse> mapToCustomerResponses(List<Object[]> rows) {
        List<CustomerSegmentResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            CustomerSegmentResponse response = CustomerSegmentResponse.builder()
                    .id(row[0] != null ? ((Number) row[0]).intValue() : null)
                    .maKhachHang(row[1] != null ? row[1].toString() : null)
                    .tenKhachHang(row[2] != null ? row[2].toString() : null)
                    .email(row[3] != null ? row[3].toString() : null)
                    .soDienThoai(row[4] != null ? row[4].toString() : null)
                    .ngaySinh(convertToLocalDate(row[5]))
                    .phanLoai(row[6] != null ? row[6].toString() : null)
                    .ordersCount(row[7] != null ? ((Number) row[7]).intValue() : 0)
                    .firstOrderAt(convertTimestamp(row[8]))
                    .lastOrderAt(convertTimestamp(row[9]))
                    .totalSpent(row[10] != null ? convertToLong(row[10]) : 0L)
                    .daysSinceLastOrder(row[11] != null ? ((Number) row[11]).intValue() : null)
                    .build();
            result.add(response);
        }
        return result;
    }

    private LocalDateTime convertTimestamp(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().atStartOfDay();
        }
        if (value instanceof LocalDate localDate) {
            return localDate.atStartOfDay();
        }
        return null;
    }

    private LocalDate convertToLocalDate(Object value) {
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        return null;
    }

    private long convertToLong(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.longValue();
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
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
