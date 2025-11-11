package org.example.be_sp.controller;

import java.util.Optional;
import org.example.be_sp.entity.OrderPayment;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.repository.OrderPaymentRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderPaymentRepository orderRepo;

    public OrderController(OrderPaymentRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @GetMapping("/{txnRef}")
    public ResponseObject<OrderPayment> getByTxnRef(@PathVariable String txnRef) {
        Optional<OrderPayment> order = orderRepo.findByTxnRef(txnRef);
        return order.map(ResponseObject::new).orElseGet(() -> ResponseObject.error("Order not found"));
    }
}


