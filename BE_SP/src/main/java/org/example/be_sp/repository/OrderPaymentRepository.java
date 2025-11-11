package org.example.be_sp.repository;

import java.util.Optional;
import org.example.be_sp.entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
    Optional<OrderPayment> findByTxnRef(String txnRef);
}


