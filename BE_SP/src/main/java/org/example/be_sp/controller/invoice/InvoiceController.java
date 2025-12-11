package org.example.be_sp.controller.invoice;

import org.example.be_sp.model.request.invoice.InvoiceAddressChangeRequest;
import org.example.be_sp.model.request.invoice.InvoiceRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.invoice.InvoiceService;
import org.example.be_sp.model.response.invoice.InvoiceResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

@RestController
@RequestMapping("/api/invoice-management")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(invoiceService.getAll(), "Lấy danh sách hóa đơn thành công");
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(invoiceService.paging(page, size), "Phân trang hóa đơn thành công");
    }

    @GetMapping("/{id}")
    public ResponseObject<?> getById(@PathVariable Integer id) {
        return new ResponseObject<>(invoiceService.getById(id), "Lấy chi tiết hóa đơn thành công");
    }

    @GetMapping("/code/{code}")
    public ResponseObject<?> getByCode(@PathVariable String code) {
        return new ResponseObject<>(invoiceService.getByCode(code), "Lấy chi tiết hóa đơn thành công");
    }

    @PostMapping("/add")
    public ResponseObject<?> create(@RequestBody InvoiceRequest request) {
        return new ResponseObject<>(invoiceService.create(request), "Thêm hóa đơn thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable Integer id, @RequestBody InvoiceRequest request) {
        return new ResponseObject<>(invoiceService.update(id, request), "Cập nhật hóa đơn thành công");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable Integer id) {
        invoiceService.delete(id);
        return new ResponseObject<>(true, null, "Xóa hóa đơn thành công");
    }

    @PostMapping("/{id}/sync-payment")
    public ResponseObject<?> synchronisePaidAmount(@PathVariable Integer id) {
        return new ResponseObject<>(invoiceService.synchronisePaidAmount(id),
                "Đồng bộ số tiền đã thanh toán thành công");
    }

    @PostMapping("/send-address-change-notification/{id}")
    public ResponseObject<?> sendAddressChangeNotification(
            @PathVariable Integer id,
            @RequestBody InvoiceAddressChangeRequest request) {
        invoiceService.sendAddressChangeNotification(id, request);
        return new ResponseObject<>(true, null, "Xử lý thay đổi địa chỉ giao hàng thành công");
    }

    @PostMapping("/{id}/confirm-delivery")
    public ResponseObject<?> confirmDelivery(@PathVariable Integer id) {
        return new ResponseObject<>(invoiceService.confirmDelivery(id), "Xác nhận giao hàng thành công");
    }

    @GetMapping("/{id}/confirm-delivery-email")
    public ResponseEntity<String> confirmDeliveryFromEmail(@PathVariable Integer id) {
        InvoiceResponse response = invoiceService.confirmDelivery(id);
        String orderCode = response != null ? response.getMaHoaDon() : null;
        String safeCode = orderCode != null ? HtmlUtils.htmlEscape(orderCode) : "#" + id;

        String html = "<!DOCTYPE html>"
                + "<html lang=\"vi\">"
                + "<head>"
                + "<meta charset=\"UTF-8\" />"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"
                + "<title>Xác nhận giao hàng</title>"
                + "<style>body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f7f8fa;color:#1d2129;padding:40px;}"
                + ".card{max-width:520px;margin:0 auto;background:#fff;border-radius:12px;padding:32px;box-shadow:0 12px 32px rgba(15,23,42,0.12);}"
                + "h1{font-size:22px;margin-bottom:16px;color:#00b42a;}p{margin:0 0 12px;line-height:1.6;}"
                + ".order-code{font-weight:600;color:#165dff;}a{color:#165dff;text-decoration:none;}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class=\"card\">"
                + "<h1>✅ Xác nhận thành công</h1>"
                + "<p>Đơn hàng <span class=\"order-code\">" + safeCode + "</span> đã được ghi nhận là giao thành công.</p>"
                + "<p>Cảm ơn bạn đã tin tưởng GearUp! Nếu cần hỗ trợ thêm, vui lòng liên hệ hotline 1900-1234.</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8")
                .body(html);
    }
}
