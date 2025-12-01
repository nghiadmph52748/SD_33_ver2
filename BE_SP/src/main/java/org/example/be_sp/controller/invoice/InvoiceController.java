package org.example.be_sp.controller.invoice;

import org.example.be_sp.model.request.invoice.InvoiceAddressChangeRequest;
import org.example.be_sp.model.request.invoice.InvoiceRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.invoice.InvoiceService;
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

    @PostMapping("/send-address-change-notification/{id}")
    public ResponseObject<?> sendAddressChangeNotification(
            @PathVariable Integer id,
            @RequestBody InvoiceAddressChangeRequest request) {
        invoiceService.sendAddressChangeNotification(id, request);
        return new ResponseObject<>(true, null, "Xử lý thay đổi địa chỉ giao hàng thành công");
    }
}
