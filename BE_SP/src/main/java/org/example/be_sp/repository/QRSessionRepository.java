package org.example.be_sp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.be_sp.entity.QRSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QRSessionRepository extends JpaRepository<QRSession, Long> {

        Optional<QRSession> findBySessionId(String sessionId);

        @Query("SELECT q FROM QRSession q WHERE q.status = :status AND q.expiresAt > :now")
        List<QRSession> findByStatusAndExpiresAtAfter(@Param("status") String status, @Param("now") LocalDateTime now);

        Optional<QRSession> findFirstByStatusAndExpiresAtAfterOrderByCreatedAtDesc(String status, LocalDateTime now);

        @Query(value = "SELECT TOP 1 * FROM qr_sessions q WHERE q.status = :status AND q.expires_at > :now ORDER BY q.expires_at DESC", nativeQuery = true)
        List<QRSession> findLatestActiveSessionCustom(
                        @Param("status") String status,
                        @Param("now") LocalDateTime now);

        Optional<QRSession> findByIdHoaDon_Id(Integer hoaDonId);

        List<QRSession> findAllByIdHoaDon_Id(Integer hoaDonId);

        List<QRSession> findByStatusAndExpiresAtBefore(String status, LocalDateTime time);

        Optional<QRSession> findByOrderCode(String orderCode);
}
