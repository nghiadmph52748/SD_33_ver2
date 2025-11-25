package org.example.be_sp.service;

import org.example.be_sp.model.email.OrderEmailData;
import org.example.be_sp.model.email.VoucherEmailData;
import org.example.be_sp.model.email.PromotionEmailData;
import org.springframework.scheduling.annotation.Async;

/**
 * Email Service Interface
 *
 * Provides methods for sending various types of email notifications to
 * customers All methods are executed asynchronously to prevent blocking API
 * responses
 */
public interface EmailService {

    /**
     * Send order confirmation email to customer
     *
     * @param orderData Order information for the email
     */
    void sendOrderConfirmationEmail(OrderEmailData orderData);

    /**
     * Send order status update email to customer
     *
     * @param orderData Order information including updated status
     */
    void sendOrderStatusUpdateEmail(OrderEmailData orderData);

    /**
     * Send voucher assignment email to customer Executes asynchronously using
     * emailTaskExecutor thread pool
     *
     * @param voucherData Voucher information for the email
     */
    @Async("emailTaskExecutor")
    void sendVoucherAssignmentEmail(VoucherEmailData voucherData);

    /**
     * Send promotion announcement email to customer Executes asynchronously
     * using emailTaskExecutor thread pool
     *
     * @param promotionData Promotion information for the email
     */
    @Async("emailTaskExecutor")
    void sendPromotionAnnouncementEmail(PromotionEmailData promotionData);

    /**
     * Send inventory shortage notification email to customer Informs customer
     * that store is trying to fulfill the order but some products are out of
     * stock Executes asynchronously to avoid blocking the API response
     *
     * @param orderData Order information including shortage details
     */
    @Async("emailTaskExecutor")
    void sendInventoryShortageNotificationEmail(OrderEmailData orderData);
}
