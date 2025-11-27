package org.example.be_sp.controller;

import org.example.be_sp.model.request.AddressChangeNotificationRequest;
import org.example.be_sp.model.request.BanHangTaiQuayRequest;
import org.example.be_sp.model.response.HoaDonResponse;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/hoa-don-management")
@CrossOrigin(origins = "*")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(hoaDonService.getAll());
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(hoaDonService.phanTrang(page, size));
    }

    @GetMapping("/{id}")
    public ResponseObject<?> getById(@PathVariable Integer id) {
        return new ResponseObject<>(hoaDonService.getByid(id), "Lấy chi tiết hóa đơn thành công");
    }

    @GetMapping("/code/{code}")
    public ResponseObject<?> getByCode(@PathVariable String code) {
        return new ResponseObject<>(hoaDonService.getByMaHoaDon(code), "Lấy chi tiết hóa đơn thành công");
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody BanHangTaiQuayRequest request) {
        HoaDonResponse created = hoaDonService.add(request);
        return new ResponseObject<>(created, "Thêm hóa đơn thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody BanHangTaiQuayRequest request) {
        hoaDonService.update(id, request);
        return new ResponseObject<>(true, null, "Cập nhật hóa đơn thành công");
    }

    @PutMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable Integer id) {
        hoaDonService.delete(id);
        return new ResponseObject<>(true, null, "Xóa hóa đơn thành công");
    }

    @PostMapping("/add-sample-data")
    public ResponseObject<?> addSampleData() {
        hoaDonService.addSampleData();
        return new ResponseObject<>(true, null, "Thêm dữ liệu mẫu thành công");
    }

    /**
     * API thống kê doanh thu chỉ tính các đơn hàng đã hoàn thành (idTrangThaiDonHang = 7)
     */
    @GetMapping("/statistics/revenue")
    public ResponseObject<?> getRevenueStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "day") String groupBy) {
        return new ResponseObject<>(hoaDonService.getCompletedOrderRevenue(startDate, endDate, groupBy), 
                                   "Lấy thống kê doanh thu thành công");
    }

    /**
     * API thống kê tổng quan chỉ tính đơn hàng hoàn thành
     */
    @GetMapping("/statistics/dashboard")
    public ResponseObject<?> getDashboardStatistics() {
        return new ResponseObject<>(hoaDonService.getCompletedOrderDashboard(), 
                                   "Lấy thống kê dashboard thành công");
    }

    /**
     * API thống kê theo khoảng thời gian
     */
    @GetMapping("/statistics/period")
    public ResponseObject<?> getPeriodStatistics(
            @RequestParam String period, // "today", "week", "month", "year"
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseObject<>(hoaDonService.getCompletedOrderStatisticsByPeriod(period, startDate, endDate), 
                                   "Lấy thống kê theo thời gian thành công");
    }

    /**
     * API so sánh doanh thu dự kiến vs thực tế
     */
    @GetMapping("/statistics/revenue-forecast")
    public ResponseObject<?> getRevenueForecastComparison(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseObject<>(hoaDonService.getRevenueForecastComparison(period, startDate, endDate), 
                                   "Lấy so sánh doanh thu dự kiến vs thực tế thành công");
    }

    /**
     * API cập nhật doanh thu dự kiến
     */
    @PostMapping("/statistics/set-target")
    public ResponseObject<?> setRevenueTarget(
            @RequestParam String period, // "month", "quarter", "year"
            @RequestParam String targetDate, // YYYY-MM-DD hoặc YYYY-MM hoặc YYYY
            @RequestParam BigDecimal targetAmount) {
        return new ResponseObject<>(hoaDonService.setRevenueTarget(period, targetDate, targetAmount), 
                                   "Cập nhật mục tiêu doanh thu thành công");
    }

    /**
     * API gửi thông báo thay đổi địa chỉ giao hàng
     */
    @PostMapping("/send-address-change-notification/{id}")
    public ResponseObject<?> sendAddressChangeNotification(
            @PathVariable Integer id,
            @RequestBody AddressChangeNotificationRequest request) {
        hoaDonService.sendAddressChangeNotification(id, request);
        return new ResponseObject<>(true, null, "Gửi thông báo thay đổi địa chỉ thành công");
    }

}
