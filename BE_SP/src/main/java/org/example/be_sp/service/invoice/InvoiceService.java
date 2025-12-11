package org.example.be_sp.service.invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.example.be_sp.entity.HinhThucThanhToan;
import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.HoaDonChiTiet;
import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.entity.PhieuGiamGia;
import org.example.be_sp.entity.PhieuGiamGiaCaNhan;
import org.example.be_sp.entity.ThongTinDonHang;
import org.example.be_sp.entity.TrangThaiDonHang;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.email.RefundNotificationEmailData;
import org.example.be_sp.model.request.AddressChangeNotificationRequest;
import org.example.be_sp.model.request.invoice.InvoiceAddressChangeRequest;
import org.example.be_sp.model.request.invoice.InvoiceRequest;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.model.response.invoice.InvoicePaymentSyncResponse;
import org.example.be_sp.model.response.invoice.InvoiceResponse;
import org.example.be_sp.model.response.invoice.OrderStageResponse;
import org.example.be_sp.repository.HinhThucThanhToanRepository;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.repository.PhieuGiamGiaCaNhanRepository;
import org.example.be_sp.repository.PhieuGiamGiaRepository;
import org.example.be_sp.repository.ThongTinDonHangRepository;
import org.example.be_sp.repository.TrangThaiDonHangRepository;
import org.example.be_sp.service.EmailService;
import org.example.be_sp.service.HoaDonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Invoice module service. Delegates core CRUD logic to the legacy
 * {@link HoaDonService} while providing a refined workflow for address changes,
 * surcharge/refund handling and response mapping.
 */
@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
    private static final BigDecimal VOUCHER_REFUND_THRESHOLD = new BigDecimal("40000");
    private static final String ADDRESS_CHANGE_NOTE = "Thay đổi địa chỉ giao hàng";
    private static final List<StageBlueprint> ONLINE_STAGE_BLUEPRINTS = List.of(
            new StageBlueprint(1, "pending", "Chờ xác nhận"),
            new StageBlueprint(2, "confirmed", "Đã xác nhận"),
            new StageBlueprint(3, "processing", "Đang xử lý"),
            new StageBlueprint(9, "preparing", "Đang chuẩn bị hàng"),
            new StageBlueprint(4, "shipping", "Đang giao hàng"),
            new StageBlueprint(5, "delivered", "Đã giao hàng"),
            new StageBlueprint(7, "completed", "Hoàn thành"));
    private static final List<StageBlueprint> OFFLINE_STAGE_BLUEPRINTS = List.of(
            new StageBlueprint(1, "pending", "Chờ xác nhận"),
            new StageBlueprint(7, "completed", "Hoàn thành"));
    private static final StageBlueprint CANCELLED_STAGE = new StageBlueprint(6, "cancelled", "Đã huỷ");

    private final HoaDonService hoaDonService;
    private final HoaDonRepository hoaDonRepository;
    private final HinhThucThanhToanRepository hinhThucThanhToanRepository;
    private final ThongTinDonHangRepository thongTinDonHangRepository;
    private final TrangThaiDonHangRepository trangThaiDonHangRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final PhieuGiamGiaCaNhanRepository phieuGiamGiaCaNhanRepository;
    private final EmailService emailService;
    private final InvoicePaymentHistoryService invoicePaymentHistoryService;

    @Autowired
    public InvoiceService(
            HoaDonService hoaDonService,
            HoaDonRepository hoaDonRepository,
            HinhThucThanhToanRepository hinhThucThanhToanRepository,
            ThongTinDonHangRepository thongTinDonHangRepository,
            TrangThaiDonHangRepository trangThaiDonHangRepository,
            PhieuGiamGiaRepository phieuGiamGiaRepository,
            PhieuGiamGiaCaNhanRepository phieuGiamGiaCaNhanRepository,
            EmailService emailService,
            InvoicePaymentHistoryService invoicePaymentHistoryService) {
        this.hoaDonService = hoaDonService;
        this.hoaDonRepository = hoaDonRepository;
        this.hinhThucThanhToanRepository = hinhThucThanhToanRepository;
        this.thongTinDonHangRepository = thongTinDonHangRepository;
        this.trangThaiDonHangRepository = trangThaiDonHangRepository;
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.phieuGiamGiaCaNhanRepository = phieuGiamGiaCaNhanRepository;
        this.emailService = emailService;
        this.invoicePaymentHistoryService = invoicePaymentHistoryService;
    }

    public List<InvoiceResponse> getAll() {
        return hoaDonRepository.findAll().stream().map(InvoiceResponse::new).toList();
    }

    public PagingResponse<InvoiceResponse> paging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> result = hoaDonRepository.findAll(pageable).map(InvoiceResponse::new);
        return new PagingResponse<>(result, page);
    }

    public InvoiceResponse getById(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ApiException("404", "Không tìm thấy hóa đơn"));
        return mapToInvoiceResponse(hoaDon);
    }

    public InvoiceResponse getByCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new ApiException("400", "Mã hóa đơn không hợp lệ");
        }
        HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(code.trim())
                .orElseThrow(() -> new ApiException("404", "Không tìm thấy hóa đơn"));
        return mapToInvoiceResponse(hoaDon);
    }

    public InvoiceResponse create(InvoiceRequest request) {
        InvoiceResponse response = InvoiceResponse.from(hoaDonService.add(request));
        return reload(response.getId());
    }

    public InvoiceResponse update(Integer id, InvoiceRequest request) {
        InvoiceResponse response = InvoiceResponse.from(hoaDonService.update(id, request));
        invoicePaymentHistoryService.appendAdditionalPayment(id, request);
        return reload(response.getId());
    }

    @Transactional
    public InvoiceResponse confirmDelivery(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ApiException("404", "Không tìm thấy hóa đơn"));

        Optional<ThongTinDonHang> latestStatus = thongTinDonHangRepository.findLatestByHoaDonId(id);
        Integer currentStatusId = latestStatus
                .map(info -> info.getIdTrangThaiDonHang() != null ? info.getIdTrangThaiDonHang().getId() : null)
                .orElse(null);

        if (currentStatusId != null && currentStatusId.equals(7)) {
            return reload(id);
        }

        if (currentStatusId == null || !currentStatusId.equals(5)) {
            throw new ApiException("400", "Chỉ có thể xác nhận khi đơn hàng đã ở trạng thái Đã giao hàng");
        }

        InvoiceRequest request = new InvoiceRequest();
        request.setIdTrangThaiDonHang(7);
        request.setTrangThai(true);

        if (Boolean.TRUE.equals(hoaDon.getGiaoHang())) {
            BigDecimal grandTotal = resolveGrandTotal(hoaDon);
            BigDecimal paidAmount = safe(hoaDon.getSoTienDaThanhToan());
            if (grandTotal.compareTo(BigDecimal.ZERO) > 0 && paidAmount.compareTo(grandTotal) < 0) {
                request.setSoTienDaThanhToan(grandTotal);
                if (hoaDon.getNgayThanhToan() == null) {
                    request.setNgayThanhToan(LocalDateTime.now());
                }
            }
        }

        InvoiceResponse response = InvoiceResponse.from(hoaDonService.update(id, request));
        return reload(response.getId());
    }

    public void delete(Integer id) {
        hoaDonService.delete(id);
    }

    @Transactional
    public void sendAddressChangeNotification(Integer orderId, InvoiceAddressChangeRequest request) {
        HoaDon hoaDon = hoaDonRepository.findByIdWithVoucher(orderId)
                .orElseThrow(() -> new ApiException("404", "Không tìm thấy hóa đơn"));

        Integer currentStatusId = null;
        Optional<ThongTinDonHang> latestStatus = thongTinDonHangRepository.findLatestByHoaDonId(hoaDon.getId());
        if (latestStatus.isPresent() && latestStatus.get().getIdTrangThaiDonHang() != null) {
            currentStatusId = latestStatus.get().getIdTrangThaiDonHang().getId();
        }

        if (currentStatusId != null && !currentStatusId.equals(1)) {
            throw new ApiException("Chỉ có thể thay đổi địa chỉ giao hàng khi đơn hàng đang ở trạng thái Chờ xác nhận", "400");
        }

        boolean alreadyChanged = thongTinDonHangRepository.existsByHoaDonIdAndStatusId(hoaDon.getId(), 8)
                || thongTinDonHangRepository.existsByHoaDonIdAndGhiChuKeyword(hoaDon.getId(), ADDRESS_CHANGE_NOTE);
        if (alreadyChanged) {
            throw new ApiException("Đơn hàng đã được thay đổi địa chỉ giao hàng trước đó", "400");
        }

        BigDecimal productTotal = calculateDetailTotal(hoaDon);
        BigDecimal currentShipping = safe(hoaDon.getPhiVanChuyen());
        BigDecimal discountAmount = determineDiscountAmount(hoaDon, productTotal, currentShipping);

        AddressChangeContext context = AddressChangeContext.from(hoaDon, request, productTotal, discountAmount);

        log.info("[InvoiceService] Address change processing for order {} with difference {}",
                hoaDon.getMaHoaDon(), context.difference);

        if (context.isPaidInFull) {
            if (context.difference.signum() > 0) {
                handlePaidFullWithSurcharge(hoaDon, context);
            } else if (context.difference.signum() < 0) {
                handlePaidFullWithRefund(hoaDon, context);
            } else {
                log.info("[InvoiceService] No surcharge/refund detected for fully paid order {}", hoaDon.getMaHoaDon());
            }
        } else {
            handleOutstandingBalance(hoaDon, context);
        }

        BigDecimal emailDelta = context.difference != null ? context.difference : BigDecimal.ZERO;
        InvoiceAddressChangeRequest emailRequest = context.request != null ? context.request : request;

        log.info("[InvoiceService] Dispatching address change email for order {} with delta {} (paidInFull={})",
                hoaDon.getMaHoaDon(), emailDelta, context.isPaidInFull);

        sendAddressChangeEmail(hoaDon, emailRequest, emailDelta);

        applyShippingFee(hoaDon, context);

        hoaDon.setUpdateAt(LocalDateTime.now());
        hoaDonRepository.save(hoaDon);

        appendAddressChangeStatus(hoaDon);
    }

    @Transactional
    public InvoicePaymentSyncResponse synchronisePaidAmount(Integer orderId) {
        HoaDon hoaDon = hoaDonRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("404", "Không tìm thấy hóa đơn"));

        initialiseLazyCollections(hoaDon);

        List<HinhThucThanhToan> payments = hinhThucThanhToanRepository.findByIdHoaDonAndDeleted(hoaDon, false);

        BigDecimal totalPaid = payments.stream()
                .filter(payment -> !Boolean.FALSE.equals(payment.getTrangThai()))
                .map(payment -> safe(payment.getTienMat()).add(safe(payment.getTienChuyenKhoan())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grandTotal = resolveGrandTotal(hoaDon);
        BigDecimal remaining = grandTotal.subtract(totalPaid);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        boolean fullyPaid = grandTotal.compareTo(BigDecimal.ZERO) > 0 && totalPaid.compareTo(grandTotal) >= 0;
        LocalDateTime syncTimestamp = LocalDateTime.now();

        hoaDon.setSoTienDaThanhToan(totalPaid);
        hoaDon.setSoTienConLai(remaining);
        hoaDon.setTrangThaiThanhToan(fullyPaid);
        if (fullyPaid && hoaDon.getNgayThanhToan() == null) {
            hoaDon.setNgayThanhToan(syncTimestamp);
        }
        hoaDon.setUpdateAt(syncTimestamp);
        hoaDonRepository.save(hoaDon);

        return new InvoicePaymentSyncResponse(
                hoaDon.getId(),
                grandTotal,
                totalPaid,
                remaining,
                fullyPaid,
                syncTimestamp);
    }

    private InvoiceResponse reload(Integer id) {
        HoaDon entity = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ApiException("404", "Không tìm thấy hóa đơn"));
        return mapToInvoiceResponse(entity);
    }

    private InvoiceResponse mapToInvoiceResponse(HoaDon hoaDon) {
        initialiseLazyCollections(hoaDon);
        InvoiceResponse response = new InvoiceResponse(hoaDon);
        response.setOrderStages(buildOrderStages(hoaDon));
        return response;
    }

    private List<OrderStageResponse> buildOrderStages(HoaDon hoaDon) {
        if (hoaDon == null) {
            return Collections.emptyList();
        }

        List<StageBlueprint> blueprints = Boolean.TRUE.equals(hoaDon.getGiaoHang())
                ? ONLINE_STAGE_BLUEPRINTS
                : OFFLINE_STAGE_BLUEPRINTS;

        if (blueprints.isEmpty()) {
            return Collections.emptyList();
        }

        List<ThongTinDonHang> timeline = hoaDon.getThongTinDonHangs() != null
                ? hoaDon.getThongTinDonHangs().stream()
                        .filter(entry -> entry != null && !Boolean.TRUE.equals(entry.getDeleted()))
                        .sorted(Comparator.comparing(InvoiceService::resolveTimelineTimestamp,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList()
                : Collections.emptyList();

        Map<Integer, ThongTinDonHang> firstEntryByStatus = new HashMap<>();
        for (ThongTinDonHang entry : timeline) {
            TrangThaiDonHang status = entry.getIdTrangThaiDonHang();
            if (status == null || status.getId() == null) {
                continue;
            }
            firstEntryByStatus.merge(status.getId(), entry, InvoiceService::selectEarlierEntry);
        }

        boolean cancelled = firstEntryByStatus.containsKey(CANCELLED_STAGE.statusId());

        List<OrderStageResponse> stages = new ArrayList<>();
        int highestReachedIndex = -1;

        for (int i = 0; i < blueprints.size(); i++) {
            StageBlueprint blueprint = blueprints.get(i);
            ThongTinDonHang matchedEntry = firstEntryByStatus.get(blueprint.statusId());
            boolean reached = matchedEntry != null;
            if (reached) {
                highestReachedIndex = i;
            }

            OrderStageResponse stage = new OrderStageResponse(
                    blueprint.statusId(),
                    blueprint.code(),
                    resolveStageName(blueprint, matchedEntry),
                    matchedEntry != null ? resolveTimelineTimestamp(matchedEntry) : null,
                    reached,
                    false,
                    false);
            stages.add(stage);
        }

        OrderStageResponse cancellationStage = null;
        if (cancelled) {
            ThongTinDonHang cancelledEntry = firstEntryByStatus.get(CANCELLED_STAGE.statusId());
            cancellationStage = new OrderStageResponse(
                    CANCELLED_STAGE.statusId(),
                    CANCELLED_STAGE.code(),
                    resolveStageName(CANCELLED_STAGE, cancelledEntry),
                    cancelledEntry != null ? resolveTimelineTimestamp(cancelledEntry) : null,
                    true,
                    true,
                    true);
        }

        if (cancelled) {
            for (OrderStageResponse stage : stages) {
                stage.setCompleted(stage.isReached());
                stage.setCurrent(false);
            }
            if (cancellationStage != null) {
                cancellationStage.setCompleted(true);
                cancellationStage.setCurrent(true);
                stages.add(cancellationStage);
            }
            return stages;
        }

        int currentIndex = highestReachedIndex >= 0 ? highestReachedIndex : 0;
        for (int i = 0; i < stages.size(); i++) {
            OrderStageResponse stage = stages.get(i);
            stage.setCompleted(highestReachedIndex >= 0 && i <= highestReachedIndex);
            stage.setCurrent(i == currentIndex);
        }

        if (cancellationStage != null) {
            stages.add(cancellationStage);
        }

        return stages;
    }

    private static String resolveStageName(StageBlueprint blueprint, ThongTinDonHang entry) {
        if (entry != null && entry.getIdTrangThaiDonHang() != null
                && StringUtils.hasText(entry.getIdTrangThaiDonHang().getTenTrangThaiDonHang())) {
            return entry.getIdTrangThaiDonHang().getTenTrangThaiDonHang();
        }
        return blueprint.label();
    }

    private static ThongTinDonHang selectEarlierEntry(ThongTinDonHang existing, ThongTinDonHang candidate) {
        if (existing == null) {
            return candidate;
        }
        if (candidate == null) {
            return existing;
        }

        LocalDateTime existingTime = resolveTimelineTimestamp(existing);
        LocalDateTime candidateTime = resolveTimelineTimestamp(candidate);

        if (existingTime == null) {
            return candidate;
        }
        if (candidateTime == null) {
            return existing;
        }
        return candidateTime.isBefore(existingTime) ? candidate : existing;
    }

    private static LocalDateTime resolveTimelineTimestamp(ThongTinDonHang entry) {
        if (entry == null) {
            return null;
        }
        return entry.getThoiGian();
    }

    private record StageBlueprint(int statusId, String code, String label) {

    }

    private void initialiseLazyCollections(HoaDon hoaDon) {
        if (hoaDon.getHoaDonChiTiets() != null) {
            hoaDon.getHoaDonChiTiets().size();
        }
        if (hoaDon.getHinhThucThanhToans() != null) {
            hoaDon.getHinhThucThanhToans().size();
        }
        if (hoaDon.getThongTinDonHangs() != null) {
            hoaDon.getThongTinDonHangs().size();
        }
        if (hoaDon.getTimelineDonHangs() != null) {
            hoaDon.getTimelineDonHangs().size();
        }
    }

    private void handlePaidFullWithSurcharge(HoaDon hoaDon, AddressChangeContext context) {
        BigDecimal surcharge = context.difference.abs();

        hoaDon.setPhuPhi(surcharge);
        hoaDon.setHoanPhi(BigDecimal.ZERO);

        log.info("[InvoiceService] Applied surcharge {} to order {}", surcharge, hoaDon.getMaHoaDon());

    }

    private void handlePaidFullWithRefund(HoaDon hoaDon, AddressChangeContext context) {
        BigDecimal refund = context.difference.abs();
        hoaDon.setPhuPhi(BigDecimal.ZERO);
        hoaDon.setHoanPhi(refund);

        boolean rewardAsVoucher = refund.compareTo(VOUCHER_REFUND_THRESHOLD) >= 0;
        String voucherCode = null;
        LocalDateTime voucherExpiry = null;

        if (rewardAsVoucher) {
            VoucherInfo voucher = createRefundVoucher(hoaDon, refund, context.customerName);
            voucherCode = voucher.code();
            voucherExpiry = voucher.expiry();
        }

        RefundNotificationEmailData emailData = RefundNotificationEmailData.builder()
                .customerName(context.customerName)
                .customerEmail(context.customerEmail)
                .orderCode(hoaDon.getMaHoaDon())
                .refundAmount(refund)
                .appliedToOrder(false)
                .originalTotal(context.originalTotal)
                .newTotal(context.originalTotal)
                .giftReward(!rewardAsVoucher)
                .voucherReward(rewardAsVoucher)
                .voucherCode(voucherCode)
                .voucherExpiry(voucherExpiry)
                .build();

        emailService.sendAddressChangeRefundNotificationEmail(emailData);
    }

    private void handleOutstandingBalance(HoaDon hoaDon, AddressChangeContext context) {
        BigDecimal surcharge = context.difference.signum() > 0 ? context.difference : BigDecimal.ZERO;
        BigDecimal refund = context.difference.signum() < 0 ? context.difference.abs() : BigDecimal.ZERO;

        hoaDon.setPhuPhi(surcharge);
        hoaDon.setHoanPhi(refund);

        if (refund.signum() > 0) {
            RefundNotificationEmailData emailData = RefundNotificationEmailData.builder()
                    .customerName(context.customerName)
                    .customerEmail(context.customerEmail)
                    .orderCode(hoaDon.getMaHoaDon())
                    .refundAmount(refund)
                    .appliedToOrder(true)
                    .originalTotal(context.originalTotal)
                    .newTotal(context.originalTotal)
                    .giftReward(false)
                    .voucherReward(false)
                    .build();
            emailService.sendAddressChangeRefundNotificationEmail(emailData);
        }
    }

    private void applyShippingFee(HoaDon hoaDon, AddressChangeContext context) {
        if (context.newShippingFee != null) {
            hoaDon.setPhiVanChuyen(context.newShippingFee);
        }
    }

    private BigDecimal calculateDetailTotal(HoaDon hoaDon) {
        if (hoaDon.getHoaDonChiTiets() == null || hoaDon.getHoaDonChiTiets().isEmpty()) {
            return safe(hoaDon.getTongTien());
        }

        return hoaDon.getHoaDonChiTiets().stream()
                .map(this::resolveLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal determineDiscountAmount(HoaDon hoaDon, BigDecimal productTotal, BigDecimal shippingFee) {
        BigDecimal recordedFinal = safe(hoaDon.getTongTienSauGiam());
        if (recordedFinal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountedSubtotal = recordedFinal.subtract(shippingFee);
            BigDecimal discount = productTotal.subtract(discountedSubtotal);
            if (discount.compareTo(BigDecimal.ZERO) < 0) {
                return BigDecimal.ZERO;
            }
            if (discount.compareTo(productTotal) > 0) {
                return productTotal;
            }
            return discount;
        }

        BigDecimal discountFromTongTien = safe(hoaDon.getTongTien()).subtract(productTotal);
        if (discountFromTongTien.compareTo(BigDecimal.ZERO) > 0) {
            return discountFromTongTien.min(productTotal);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal resolveGrandTotal(HoaDon hoaDon) {
        BigDecimal shipping = safe(hoaDon.getPhiVanChuyen());

        BigDecimal discounted = safe(hoaDon.getTongTienSauGiam());
        if (discounted.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal total = discounted.add(shipping);
            if (Boolean.TRUE.equals(hoaDon.getGiaoHang())) {
                total = total.add(safe(hoaDon.getHoanPhi()));
            }
            return total.compareTo(BigDecimal.ZERO) > 0 ? total : BigDecimal.ZERO;
        }

        BigDecimal detailTotal = calculateDetailTotal(hoaDon);
        if (detailTotal.compareTo(BigDecimal.ZERO) <= 0) {
            detailTotal = safe(hoaDon.getTongTien());
        }

        BigDecimal total = detailTotal.add(shipping);
        if (Boolean.TRUE.equals(hoaDon.getGiaoHang())) {
            total = total.add(safe(hoaDon.getHoanPhi()));
        }
        return total.compareTo(BigDecimal.ZERO) > 0 ? total : BigDecimal.ZERO;
    }

    private BigDecimal resolveLineTotal(HoaDonChiTiet chiTiet) {
        if (chiTiet == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal lineTotal = chiTiet.getThanhTien();
        if (lineTotal != null) {
            return lineTotal;
        }
        BigDecimal unitPrice = safe(chiTiet.getGiaBan());
        BigDecimal quantity = chiTiet.getSoLuong() != null ? BigDecimal.valueOf(chiTiet.getSoLuong()) : BigDecimal.ZERO;
        return unitPrice.multiply(quantity);
    }

    private void appendAddressChangeStatus(HoaDon hoaDon) {
        TrangThaiDonHang status = trangThaiDonHangRepository.findById(1).orElse(null);
        if (status == null) {
            log.warn("[InvoiceService] Missing TrangThaiDonHang id=1, skipping history append for order {}",
                    hoaDon.getMaHoaDon());
            return;
        }

        ThongTinDonHang thongTin = new ThongTinDonHang();
        thongTin.setIdHoaDon(hoaDon);
        thongTin.setIdTrangThaiDonHang(status);
        thongTin.setTrangThai(true);
        thongTin.setThoiGian(LocalDateTime.now());
        thongTin.setDeleted(false);
        thongTin.setGhiChu(ADDRESS_CHANGE_NOTE);
        thongTinDonHangRepository.save(thongTin);
    }

    private void sendAddressChangeEmail(HoaDon hoaDon, InvoiceAddressChangeRequest request, BigDecimal surcharge) {
        if (!StringUtils.hasText(hoaDon.getEmailNguoiNhan()) && (hoaDon.getIdKhachHang() == null
                || !StringUtils.hasText(hoaDon.getIdKhachHang().getEmail()))) {
            log.warn("[InvoiceService] Skipping address change email for order {} due to missing customer email",
                    hoaDon.getMaHoaDon());
            return;
        }

        String customerEmail = StringUtils.hasText(hoaDon.getEmailNguoiNhan())
                ? hoaDon.getEmailNguoiNhan()
                : hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getEmail() : null;
        String customerName = resolveCustomerName(hoaDon);

        String oldAddress = buildAddressString(request != null ? request.getOldAddress() : null);
        String newAddress = buildAddressString(request != null ? request.getNewAddress() : null);

        log.info("[InvoiceService] Prepared address change email payload for order {} | old='{}' | new='{}' | delta={}",
                hoaDon.getMaHoaDon(), oldAddress, newAddress, surcharge);

        emailService.sendAddressChangeNotificationEmail(
                customerEmail,
                customerName,
                hoaDon.getMaHoaDon(),
                oldAddress,
                newAddress,
                surcharge != null ? surcharge : BigDecimal.ZERO);
    }

    private String resolveCustomerName(HoaDon hoaDon) {
        if (StringUtils.hasText(hoaDon.getTenNguoiNhan())) {
            return hoaDon.getTenNguoiNhan();
        }
        KhachHang khachHang = hoaDon.getIdKhachHang();
        if (khachHang != null && StringUtils.hasText(khachHang.getTenKhachHang())) {
            return khachHang.getTenKhachHang();
        }
        return "Khách hàng";
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private VoucherInfo createRefundVoucher(HoaDon hoaDon, BigDecimal value, String customerName) {
        String voucherCode = generateVoucherCode(hoaDon.getId(), "REF");
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiry = issuedAt.plusMonths(3);

        PhieuGiamGia voucher = new PhieuGiamGia();
        voucher.setMaPhieuGiamGia(voucherCode);
        voucher.setTenPhieuGiamGia("Refund voucher - " + customerName);
        voucher.setLoaiPhieuGiamGia(true);
        voucher.setGiaTriGiamGia(value);
        voucher.setHoaDonToiThieu(BigDecimal.ZERO);
        voucher.setSoLuongDung(1);
        voucher.setNgayBatDau(issuedAt);
        voucher.setNgayKetThuc(expiry);
        voucher.setTrangThai(true);
        voucher.setDeleted(false);
        voucher.setFeatured(hoaDon.getIdKhachHang() != null);
        voucher.setMoTa("Voucher hoàn phí do thay đổi địa chỉ giao hàng");
        voucher.setCreatedAt(issuedAt);
        voucher.setUpdatedAt(issuedAt);

        PhieuGiamGia saved = phieuGiamGiaRepository.save(voucher);

        if (hoaDon.getIdKhachHang() != null) {
            PhieuGiamGiaCaNhan personal = new PhieuGiamGiaCaNhan();
            personal.setIdKhachHang(hoaDon.getIdKhachHang());
            personal.setIdPhieuGiamGia(saved);
            personal.setTenPhieuGiamGiaCaNhan("Refund voucher - " + customerName);
            personal.setNgayNhan(issuedAt);
            personal.setNgayHetHan(expiry);
            personal.setTrangThai(true);
            personal.setDeleted(false);
            phieuGiamGiaCaNhanRepository.save(personal);
        }

        return new VoucherInfo(voucherCode, expiry);
    }

    private String generateVoucherCode(Integer orderId, String prefix) {
        String normalizedPrefix = StringUtils.hasText(prefix) ? prefix.toUpperCase() : "INV";
        return String.format("%s_%d_%d", normalizedPrefix, orderId, System.currentTimeMillis() % 100000);
    }

    private String buildAddressString(AddressChangeNotificationRequest.AddressInfo address) {
        if (address == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        appendAddressPart(builder, address.getDiaChiCuThe());
        appendAddressPart(builder, address.getPhuong());
        appendAddressPart(builder, address.getQuan());
        appendAddressPart(builder, address.getThanhPho());
        return builder.toString();
    }

    private void appendAddressPart(StringBuilder builder, String part) {
        if (!StringUtils.hasText(part)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(part.trim());
    }

    private static final class AddressChangeContext {

        private final InvoiceAddressChangeRequest request;
        private final BigDecimal originalTotal;
        private final BigDecimal productTotal;
        private final BigDecimal discountAmount;
        private final BigDecimal difference;
        private final BigDecimal currentShippingFee;
        private final BigDecimal newShippingFee;
        private final boolean isPaidInFull;
        private final String customerEmail;
        private final String customerName;

        private AddressChangeContext(
                InvoiceAddressChangeRequest request,
                BigDecimal originalTotal,
                BigDecimal productTotal,
                BigDecimal discountAmount,
                BigDecimal difference,
                BigDecimal currentShippingFee,
                BigDecimal newShippingFee,
                boolean paidInFull,
                String customerEmail,
                String customerName) {
            this.request = request;
            this.originalTotal = originalTotal;
            this.productTotal = productTotal;
            this.discountAmount = discountAmount;
            this.difference = difference;
            this.currentShippingFee = currentShippingFee;
            this.newShippingFee = newShippingFee;
            this.isPaidInFull = paidInFull;
            this.customerEmail = customerEmail;
            this.customerName = customerName;
        }

        private static AddressChangeContext from(
                HoaDon hoaDon,
                InvoiceAddressChangeRequest request,
                BigDecimal productTotal,
                BigDecimal discountAmount) {
            BigDecimal originalTotal = hoaDon.getTongTienSauGiam() != null
                    ? hoaDon.getTongTienSauGiam()
                    : BigDecimal.ZERO;

            BigDecimal currentFee = request != null && request.getShippingFeeChange() != null
                    && request.getShippingFeeChange().getCurrentFee() != null
                    ? request.getShippingFeeChange().getCurrentFee()
                    : hoaDon.getPhiVanChuyen();
            BigDecimal newFee = request != null && request.getShippingFeeChange() != null
                    && request.getShippingFeeChange().getNewFee() != null
                    ? request.getShippingFeeChange().getNewFee()
                    : currentFee;

            BigDecimal difference = BigDecimal.ZERO;
            if (request != null && request.getShippingFeeChange() != null
                    && request.getShippingFeeChange().getDifference() != null) {
                difference = request.getShippingFeeChange().getDifference();
            } else if (newFee != null && currentFee != null) {
                difference = newFee.subtract(currentFee);
            } else if (request != null && request.getSurcharge() != null) {
                difference = request.getSurcharge();
            }

            BigDecimal paidAmount = hoaDon.getSoTienDaThanhToan() != null
                    ? hoaDon.getSoTienDaThanhToan()
                    : BigDecimal.ZERO;
            boolean paidInFull = paidAmount.compareTo(originalTotal) >= 0;

            String customerEmail = resolveEmail(hoaDon);
            String customerName = resolveName(hoaDon);

            return new AddressChangeContext(request, originalTotal, productTotal, discountAmount, difference, currentFee, newFee,
                    paidInFull, customerEmail,
                    customerName);
        }

        private static String resolveEmail(HoaDon hoaDon) {
            if (StringUtils.hasText(hoaDon.getEmailNguoiNhan())) {
                return hoaDon.getEmailNguoiNhan();
            }
            return hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getEmail() : null;
        }

        private static String resolveName(HoaDon hoaDon) {
            if (StringUtils.hasText(hoaDon.getTenNguoiNhan())) {
                return hoaDon.getTenNguoiNhan();
            }
            return hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getTenKhachHang() : "Khách hàng";
        }

        @SuppressWarnings("unused")
        BigDecimal calculateDiscountedTotal() {
            BigDecimal subtotalAfterDiscount = safe(productTotal).subtract(safe(discountAmount));
            if (subtotalAfterDiscount.compareTo(BigDecimal.ZERO) < 0) {
                subtotalAfterDiscount = BigDecimal.ZERO;
            }
            BigDecimal total = subtotalAfterDiscount.add(targetShippingFee());
            return total.compareTo(BigDecimal.ZERO) > 0 ? total : BigDecimal.ZERO;
        }

        private BigDecimal targetShippingFee() {
            BigDecimal target = newShippingFee != null ? newShippingFee : currentShippingFee;
            return safe(target);
        }
    }

    private record VoucherInfo(String code, LocalDateTime expiry) {

    }
}
