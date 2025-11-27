package org.example.be_sp.model.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSegmentResponse {
    private Integer id;
    private String maKhachHang;
    private String tenKhachHang;
    private String email;
    private String soDienThoai;
    private LocalDate ngaySinh;
    private String phanLoai;
    private Integer ordersCount;
    private LocalDateTime firstOrderAt;
    private LocalDateTime lastOrderAt;
    private Long totalSpent;
    private Integer daysSinceLastOrder;
}

