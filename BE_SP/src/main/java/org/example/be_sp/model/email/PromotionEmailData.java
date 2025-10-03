package org.example.be_sp.model.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * Email data transfer object for promotion announcement emails
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionEmailData {
    private String customerName;
    private String customerEmail;
    private String promotionName;
    private Integer discountPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private List<String> featuredProducts;
    private String shopNowUrl;
}
