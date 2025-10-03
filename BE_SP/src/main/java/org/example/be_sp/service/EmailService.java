package org.example.be_sp.service;

import org.example.be_sp.model.email.OrderEmailData;
import org.example.be_sp.model.email.VoucherEmailData;
import org.example.be_sp.model.email.PromotionEmailData;

/**
 * Email Service Interface
 * 
 * Provides methods for sending various types of email notifications to customers
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
     * Send voucher assignment email to customer
     * 
     * @param voucherData Voucher information for the email
     */
    void sendVoucherAssignmentEmail(VoucherEmailData voucherData);
    
    /**
     * Send promotion announcement email to customer
     * 
     * @param promotionData Promotion information for the email
     */
    void sendPromotionAnnouncementEmail(PromotionEmailData promotionData);
}
