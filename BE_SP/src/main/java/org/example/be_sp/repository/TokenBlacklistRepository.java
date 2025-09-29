package org.example.be_sp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.be_sp.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByToken(String token);

    Optional<TokenBlacklist> findByToken(String token);

    @Query("SELECT t FROM TokenBlacklist t WHERE t.expiryDate < :now")
    List<TokenBlacklist> findExpiredTokens(@Param("now") LocalDateTime now);

    void deleteByToken(String token);

    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
