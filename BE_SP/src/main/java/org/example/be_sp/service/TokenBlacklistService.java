package org.example.be_sp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.example.be_sp.entity.TokenBlacklist;
import org.example.be_sp.repository.TokenBlacklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

@Service
public class TokenBlacklistService {

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initializeDatabase() {
        try {
            // Check if table exists
            jdbcTemplate.execute("SELECT 1 FROM token_blacklist");
        } catch (Exception e) {
            // Table doesn't exist, create it
            try {
                String createTableSql = """
                    CREATE TABLE token_blacklist (
                        id BIGINT IDENTITY(1,1) PRIMARY KEY,
                        token NVARCHAR(255) NOT NULL,
                        username NVARCHAR(255) NOT NULL,
                        expiry_date DATETIME2 NOT NULL,
                        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),

                        INDEX idx_token_blacklist_token (token),
                        INDEX idx_token_blacklist_username (username),
                        INDEX idx_token_blacklist_expiry_date (expiry_date)
                    );

                    ALTER TABLE token_blacklist
                    ADD CONSTRAINT uk_token_blacklist_token UNIQUE (token);
                    """;

                jdbcTemplate.execute(createTableSql);
            } catch (Exception createException) {
                System.err.println("Failed to create token blacklist table: " + createException.getMessage());
            }
        }
    }

    /**
     * Add token to blacklist
     */
    @Transactional
    public void addToBlacklist(String token, String username, LocalDateTime expiryDate) {
        if (!tokenBlacklistRepository.existsByToken(token)) {
            TokenBlacklist blacklistedToken = new TokenBlacklist(token, username, expiryDate);
            tokenBlacklistRepository.save(blacklistedToken);
        }
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    /**
     * Remove token from blacklist
     */
    @Transactional
    public void removeFromBlacklist(String token) {
        tokenBlacklistRepository.deleteByToken(token);
    }

    /**
     * Clean up expired tokens - run every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour = 3600000 milliseconds
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        List<TokenBlacklist> expiredTokens = tokenBlacklistRepository.findExpiredTokens(now);
        if (!expiredTokens.isEmpty()) {
            tokenBlacklistRepository.deleteExpiredTokens(now);
        }
    }

    /**
     * Get all blacklisted tokens (for admin purposes)
     */
    public List<TokenBlacklist> getAllBlacklistedTokens() {
        return tokenBlacklistRepository.findAll();
    }

    /**
     * Get blacklisted tokens for a specific user
     */
    public List<TokenBlacklist> getBlacklistedTokensByUsername(String username) {
        return tokenBlacklistRepository.findAll().stream()
                .filter(token -> username.equals(token.getUsername()))
                .toList();
    }
}
