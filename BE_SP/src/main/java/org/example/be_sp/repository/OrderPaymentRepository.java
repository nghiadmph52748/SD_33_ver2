package org.example.be_sp.repository;

import java.util.List;
import java.util.Optional;
import org.example.be_sp.entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
    Optional<OrderPayment> findByTxnRef(String txnRef);
    
    @Query("SELECT op FROM OrderPayment op WHERE op.status = 'PENDING' AND op.txnRef LIKE :orderIdPrefix%")
    List<OrderPayment> findPendingByOrderIdPrefix(@Param("orderIdPrefix") String orderIdPrefix);
}


