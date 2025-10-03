package org.example.be_sp.controller;

import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.HoaDonService;
import org.example.be_sp.service.KhachHangService;
import org.example.be_sp.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private KhachHangService khachHangService;

    @GetMapping("/database")
    public ResponseObject<?> testDatabase() {
        try {
            // Test database connection by counting records
            int invoiceCount = hoaDonService.getAll().size();
            int productCount = sanPhamService.getAll().size();
            int customerCount = khachHangService.findAll().size();

            Object result = new Object() {
                public final int invoices = invoiceCount;
                public final int products = productCount;
                public final int customers = customerCount;
                public final String status = "Database connected successfully";
                public final long timestamp = System.currentTimeMillis();
            };

            return new ResponseObject<>(true, result, "Database test successful");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Database test failed: " + e.getMessage());
        }
    }

    @GetMapping("/invoices")
    public ResponseObject<?> testInvoices() {
        try {
            var invoices = hoaDonService.getAll();
            return new ResponseObject<>(true, invoices, "Invoices loaded successfully");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Failed to load invoices: " + e.getMessage());
        }
    }

    @GetMapping("/products")
    public ResponseObject<?> testProducts() {
        try {
            var products = sanPhamService.getAll();
            return new ResponseObject<>(true, products, "Products loaded successfully");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Failed to load products: " + e.getMessage());
        }
    }

    @GetMapping("/customers")
    public ResponseObject<?> testCustomers() {
        try {
            var customers = khachHangService.findAll();
            return new ResponseObject<>(true, customers, "Customers loaded successfully");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, "Failed to load customers: " + e.getMessage());
        }
    }
}
