package org.example.be_sp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be_sp.config.EmailConfig;
import org.example.be_sp.model.email.OrderEmailData;
import org.example.be_sp.model.email.PromotionEmailData;
import org.example.be_sp.model.email.VoucherEmailData;
import org.example.be_sp.service.EmailService;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Email Service Implementation
 *
 * Handles sending HTML emails using JavaMailSender and Thymeleaf templates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailConfig emailConfig;

    @Override
    public void sendOrderConfirmationEmail(OrderEmailData orderData) {
        if (!emailConfig.isEnabled()) {
            log.info("Email disabled. Skipping order confirmation email for order: {}",
                    orderData.getOrderCode());
            return;
        }

        try {
            log.info("Sending order confirmation email to: {} for order: {}",
                    orderData.getCustomerEmail(), orderData.getOrderCode());

            Context context = new Context();
            context.setVariable("order", orderData);
            context.setVariable("baseUrl", emailConfig.getBaseUrl());

            String htmlContent = templateEngine.process("email/order-confirmation", context);

            sendHtmlEmail(
                    orderData.getCustomerEmail(),
                    "Xác nhận đơn hàng #" + orderData.getOrderCode(),
                    htmlContent
            );

            log.info("Order confirmation email sent successfully to: {}",
                    orderData.getCustomerEmail());

        } catch (MailException | MessagingException e) {
            log.error("Failed to send order confirmation email to: {} for order: {}",
                    orderData.getCustomerEmail(), orderData.getOrderCode(), e);
            // Don't throw exception - we don't want to rollback the transaction
        }
    }

    @Override
    public void sendOrderStatusUpdateEmail(OrderEmailData orderData) {
        if (!emailConfig.isEnabled()) {
            log.info("Email disabled. Skipping order status update email for order: {}",
                    orderData.getOrderCode());
            return;
        }

        try {
            log.info("Sending order status update email to: {} for order: {} - Status: {}",
                    orderData.getCustomerEmail(), orderData.getOrderCode(), orderData.getOrderStatus());

            Context context = new Context();
            context.setVariable("order", orderData);
            context.setVariable("baseUrl", emailConfig.getBaseUrl());

            String htmlContent = templateEngine.process("email/order-status-update", context);

            sendHtmlEmail(
                    orderData.getCustomerEmail(),
                    "Cập nhật đơn hàng #" + orderData.getOrderCode() + " - " + orderData.getOrderStatus(),
                    htmlContent
            );

            log.info("Order status update email sent successfully to: {}",
                    orderData.getCustomerEmail());

        } catch (MailException | MessagingException e) {
            log.error("Failed to send order status update email to: {} for order: {}",
                    orderData.getCustomerEmail(), orderData.getOrderCode(), e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendVoucherAssignmentEmail(VoucherEmailData voucherData) {
        String threadName = Thread.currentThread().getName();

        if (!emailConfig.isEnabled()) {
            log.info("[{}] Email disabled. Skipping voucher assignment email for: {}",
                    threadName, voucherData.getCustomerEmail());
            return;
        }

        try {
            log.info("[{}] Sending voucher assignment email to: {} - Voucher: {}",
                    threadName, voucherData.getCustomerEmail(), voucherData.getVoucherCode());

            Context context = new Context();
            context.setVariable("voucher", voucherData);
            context.setVariable("baseUrl", emailConfig.getBaseUrl());

            String htmlContent = templateEngine.process("email/voucher-assignment", context);

            sendHtmlEmail(
                    voucherData.getCustomerEmail(),
                    "🎁 Bạn nhận được phiếu giảm giá mới từ GearUp!",
                    htmlContent
            );

            log.info("[{}] Voucher assignment email sent successfully to: {}",
                    threadName, voucherData.getCustomerEmail());

        } catch (MailException | MessagingException e) {
            log.error("[{}] Failed to send voucher assignment email to: {} - Voucher: {} - Error: {}",
                    threadName, voucherData.getCustomerEmail(), voucherData.getVoucherCode(), e.getMessage(), e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendPromotionAnnouncementEmail(PromotionEmailData promotionData) {
        String threadName = Thread.currentThread().getName();

        if (!emailConfig.isEnabled()) {
            log.info("[{}] Email disabled. Skipping promotion announcement email for: {}",
                    threadName, promotionData.getCustomerEmail());
            return;
        }

        try {
            log.info("[{}] Sending promotion announcement email to: {} - Promotion: {}",
                    threadName, promotionData.getCustomerEmail(), promotionData.getPromotionName());

            Context context = new Context();
            context.setVariable("promotion", promotionData);
            context.setVariable("baseUrl", emailConfig.getBaseUrl());

            String htmlContent = templateEngine.process("email/promotion-announcement", context);

            sendHtmlEmail(
                    promotionData.getCustomerEmail(),
                    "🔥 " + promotionData.getPromotionName(),
                    htmlContent
            );

            log.info("[{}] Promotion announcement email sent successfully to: {}",
                    threadName, promotionData.getCustomerEmail());

        } catch (MailException | MessagingException e) {
            log.error("[{}] Failed to send promotion announcement email to: {} - Promotion: {} - Error: {}",
                    threadName, promotionData.getCustomerEmail(), promotionData.getPromotionName(), e.getMessage(), e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendInventoryShortageNotificationEmail(OrderEmailData orderData) {
        String threadName = Thread.currentThread().getName();

        if (!emailConfig.isEnabled()) {
            log.info("[{}] Email disabled. Skipping inventory shortage notification for order: {}",
                    threadName, orderData.getOrderCode());
            return;
        }

        try {
            log.info("[{}] Sending inventory shortage notification email to: {} for order: {}",
                    threadName, orderData.getCustomerEmail(), orderData.getOrderCode());

            Context context = new Context();
            context.setVariable("order", orderData);
            context.setVariable("baseUrl", emailConfig.getBaseUrl());

            String htmlContent = templateEngine.process("email/inventory-shortage-notification", context);

            sendHtmlEmail(
                    orderData.getCustomerEmail(),
                    "⚠️ Thông báo - Sự cố về số lượng sản phẩm đơn hàng #" + orderData.getOrderCode(),
                    htmlContent
            );

            log.info("[{}] Inventory shortage notification email sent successfully to: {}",
                    threadName, orderData.getCustomerEmail());

        } catch (MailException | MessagingException e) {
            log.error("[{}] Failed to send inventory shortage notification email to: {} for order: {} - Error: {}",
                    threadName, orderData.getCustomerEmail(), orderData.getOrderCode(), e.getMessage(), e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendAddressChangeNotificationEmail(
            String customerEmail,
            String customerName,
            String orderCode,
            String oldAddress,
            String newAddress,
            java.math.BigDecimal surcharge) {
        
        String threadName = Thread.currentThread().getName();

        if (!emailConfig.isEnabled()) {
            log.info("[{}] Email disabled. Skipping address change notification for order: {}",
                    threadName, orderCode);
            return;
        }

        try {
            log.info("[{}] Sending address change notification email to: {} for order: {}",
                    threadName, customerEmail, orderCode);

            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("orderCode", orderCode);
            context.setVariable("oldAddress", oldAddress);
            context.setVariable("newAddress", newAddress);
            context.setVariable("surcharge", surcharge);
            context.setVariable("surchargeFormatted", String.format("%,d", surcharge.toBigInteger()));
            context.setVariable("baseUrl", emailConfig.getBaseUrl());

            String htmlContent = templateEngine.process("email/address-change-notification", context);

            sendHtmlEmail(
                    customerEmail,
                    "📍 Thông báo thay đổi địa chỉ giao hàng - Đơn hàng #" + orderCode,
                    htmlContent
            );

            log.info("[{}] Address change notification email sent successfully to: {}",
                    threadName, customerEmail);

        } catch (MailException | MessagingException e) {
            log.error("[{}] Failed to send address change notification email to: {} for order: {} - Error: {}",
                    threadName, customerEmail, orderCode, e.getMessage(), e);
        }
    }

    /**
     * Helper method to send HTML email
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlContent HTML content of the email
     * @throws MessagingException if email sending fails
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent)
            throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFromAddress(), emailConfig.getFromPersonal());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new MessagingException("Unsupported encoding", e);
        }
    }
}
