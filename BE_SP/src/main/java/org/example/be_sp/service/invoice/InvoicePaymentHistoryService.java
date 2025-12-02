package org.example.be_sp.service.invoice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.example.be_sp.entity.HinhThucThanhToan;
import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.PhuongThucThanhToan;
import org.example.be_sp.model.request.BanHangTaiQuayRequest;
import org.example.be_sp.repository.HinhThucThanhToanRepository;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.repository.PhuongThucThanhToanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InvoicePaymentHistoryService {

    private static final Logger log = LoggerFactory.getLogger(InvoicePaymentHistoryService.class);

    private final HoaDonRepository hoaDonRepository;
    private final HinhThucThanhToanRepository hinhThucThanhToanRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    public InvoicePaymentHistoryService(
            HoaDonRepository hoaDonRepository,
            HinhThucThanhToanRepository hinhThucThanhToanRepository,
            PhuongThucThanhToanRepository phuongThucThanhToanRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.hinhThucThanhToanRepository = hinhThucThanhToanRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
    }

    public void appendAdditionalPayment(Integer orderId, BanHangTaiQuayRequest request) {
        if (orderId == null || request == null) {
            return;
        }

        BigDecimal additionalPaid = request.getSoTienThuThem();
        if (additionalPaid == null || additionalPaid.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        HoaDon invoice = hoaDonRepository.findById(orderId).orElse(null);
        if (invoice == null) {
            log.warn("[InvoicePaymentHistory] Unable to find invoice {} to append payment history", orderId);
            return;
        }

        try {
            PhuongThucThanhToan paymentMethod = resolvePaymentMethod(invoice, request);
            if (paymentMethod == null) {
                log.warn("[InvoicePaymentHistory] No payment method resolved for invoice {}", orderId);
                return;
            }

            HinhThucThanhToan paymentRecord = new HinhThucThanhToan();
            paymentRecord.setIdHoaDon(invoice);
            paymentRecord.setIdPhuongThucThanhToan(paymentMethod);

            if (isCashLikeMethod(paymentMethod)) {
                paymentRecord.setTienMat(additionalPaid);
                paymentRecord.setTienChuyenKhoan(BigDecimal.ZERO);
            } else {
                paymentRecord.setTienChuyenKhoan(additionalPaid);
                paymentRecord.setTienMat(BigDecimal.ZERO);
            }
            paymentRecord.setTrangThai(true);
            paymentRecord.setDeleted(false);

            hinhThucThanhToanRepository.save(paymentRecord);
        } catch (Exception ex) {
            log.error("[InvoicePaymentHistory] Failed to append payment history for invoice {}: {}", orderId, ex.getMessage(), ex);
        }
    }

    private PhuongThucThanhToan resolvePaymentMethod(HoaDon invoice, BanHangTaiQuayRequest request) {
        if (request.getIdPhuongThucThanhToan() != null) {
            return phuongThucThanhToanRepository.findById(request.getIdPhuongThucThanhToan()).orElse(null);
        }

        List<HinhThucThanhToan> existingPayments = hinhThucThanhToanRepository.findByIdHoaDonAndDeleted(invoice, false);
        if (existingPayments != null && !existingPayments.isEmpty()) {
            HinhThucThanhToan primaryPayment = existingPayments.get(0);
            if (primaryPayment != null && primaryPayment.getIdPhuongThucThanhToan() != null) {
                return primaryPayment.getIdPhuongThucThanhToan();
            }
        }

        return phuongThucThanhToanRepository.findById(1).orElse(null);
    }

    private boolean isCashLikeMethod(PhuongThucThanhToan method) {
        if (method == null) {
            return false;
        }

        String methodName = method.getTenPhuongThucThanhToan();
        if (methodName != null && !methodName.isBlank()) {
            String normalized = methodName.trim().toLowerCase(Locale.ROOT);
            if (normalized.contains("tiền mặt")
                    || normalized.contains("tien mat")
                    || normalized.contains("cash")
                    || normalized.contains("cod")) {
                return true;
            }
        }

        Integer methodId = method.getId();
        return methodId != null && methodId == 1;
    }
}
