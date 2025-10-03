package org.example.be_sp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be_sp.model.email.PromotionEmailData;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.EmailService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Test controller for email functionality
 * Remove this in production
 */
@RestController
@RequestMapping("/api/email-test")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class EmailTestController {
    
    private final EmailService emailService;
    
    @PostMapping("/send-promotion")
    public ResponseObject<?> sendPromotionEmail(@RequestParam String email) {
        try {
            log.info("Sending test promotion email to: {}", email);
            
            PromotionEmailData promotionData = PromotionEmailData.builder()
                .customerName("Hoàng Duy")
                .customerEmail(email)
                .promotionName("MEGA SALE THÁNG 10 - GIẢM GIÁ ĐẾN 50%")
                .discountPercentage(50)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .description("Chào mừng tháng 10, GearUp mang đến chương trình khuyến mãi khủng với mức giảm giá lên đến 50% cho tất cả sản phẩm giày thể thao. Đây là cơ hội tuyệt vời để bạn sở hữu những đôi giày yêu thích với giá ưu đãi nhất trong năm!")
                .featuredProducts(Arrays.asList(
                    "Nike Air Max 2024 - Giảm 45%",
                    "Adidas Ultraboost - Giảm 40%",
                    "Puma RS-X - Giảm 50%",
                    "New Balance 990v5 - Giảm 35%"
                ))
                .shopNowUrl("/products")
                .build();
            
            emailService.sendPromotionAnnouncementEmail(promotionData);
            
            return new ResponseObject<>(null, "Email promotion đã được gửi thành công đến: " + email);
            
        } catch (Exception e) {
            log.error("Error sending promotion email", e);
            return ResponseObject.error("Lỗi khi gửi email: " + e.getMessage());
        }
    }
}
