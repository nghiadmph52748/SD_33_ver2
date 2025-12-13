package org.example.be_sp.service;

import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final KhachHangRepository khachHangRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${oauth.google.client-id:}")
    private String googleClientId;

    @Value("${oauth.facebook.app-id:}")
    private String facebookAppId;

    @Value("${oauth.facebook.app-secret:}")
    private String facebookAppSecret;

    public OAuthUserInfo verifyGoogleToken(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Google token verification failed: {}", response.getStatusCode());
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String email = jsonNode.has("email") ? jsonNode.get("email").asText() : null;
            String name = jsonNode.has("name") ? jsonNode.get("name").asText() : null;
            String sub = jsonNode.has("sub") ? jsonNode.get("sub").asText() : null;

            if (sub == null || sub.isBlank()) {
                log.error("Google token missing sub (user ID)");
                return null;
            }

            // If email is missing, we'll generate one in findOrCreateUser
            // But prefer to have email if available
            return new OAuthUserInfo(email, name, sub, "google");
        } catch (Exception e) {
            log.error("Error verifying Google token", e);
            return null;
        }
    }

    public OAuthUserInfo verifyFacebookToken(String accessToken) {
        try {
            String url = String.format(
                "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=%s",
                accessToken
            );
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Facebook token verification failed: {}", response.getStatusCode());
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            if (jsonNode.has("error")) {
                log.error("Facebook API error: {}", jsonNode.get("error"));
                return null;
            }

            String email = jsonNode.has("email") ? jsonNode.get("email").asText() : null;
            String name = jsonNode.has("name") ? jsonNode.get("name").asText() : null;
            String id = jsonNode.has("id") ? jsonNode.get("id").asText() : null;

            if (id == null || id.isBlank()) {
                log.error("Facebook token missing user ID");
                return null;
            }

            if (email == null || email.isBlank()) {
                email = id + "@facebook.com";
            }

            return new OAuthUserInfo(email, name, id, "facebook");
        } catch (Exception e) {
            log.error("Error verifying Facebook token", e);
            return null;
        }
    }

    @Transactional
    public KhachHang findOrCreateUser(OAuthUserInfo userInfo) {
        // Validate provider ID
        if (userInfo.getProviderId() == null || userInfo.getProviderId().isBlank()) {
            log.error("OAuth user info missing provider ID");
            throw new IllegalArgumentException("Provider ID is required");
        }

        // Ensure email is never null - use fallback if needed
        String email = userInfo.getEmail();
        if (email == null || email.isBlank()) {
            // Generate a unique email using provider ID and provider name
            String providerId = userInfo.getProviderId();
            String provider = userInfo.getProvider() != null ? userInfo.getProvider() : "oauth";
            email = providerId + "@" + provider + ".oauth.local";
            log.info("Generated email for OAuth user: {}", email);
        }
        email = email.toLowerCase().trim();
        
        // Ensure email is valid (not empty after trim)
        if (email.isBlank()) {
            throw new IllegalArgumentException("Cannot create user with blank email");
        }
        
        // Check if user exists by email
        KhachHang existing = khachHangRepository.findByEmail(email);
        if (existing != null) {
            log.info("Found existing user by email: {}", email);
            if (!Boolean.TRUE.equals(existing.getTrangThai())) {
                existing.setTrangThai(true);
                khachHangRepository.save(existing);
            }
            return existing;
        }

        // Also check by ten_tai_khoan to avoid unique constraint violation
        KhachHang existingByUsername = khachHangRepository.findByTenTaiKhoan(email);
        if (existingByUsername != null) {
            log.info("Found existing user by username: {}", email);
            // Update email if it's null or blank
            if (existingByUsername.getEmail() == null || existingByUsername.getEmail().isBlank()) {
                existingByUsername.setEmail(email);
                khachHangRepository.save(existingByUsername);
            }
            if (!Boolean.TRUE.equals(existingByUsername.getTrangThai())) {
                existingByUsername.setTrangThai(true);
                khachHangRepository.save(existingByUsername);
            }
            return existingByUsername;
        }

        // Before creating, check for any users with NULL email or ten_tai_khoan that we should update
        // This handles the case where there's already a user with NULL in the unique column
        // SQL Server only allows one NULL in a unique column, so we need to update existing NULL users
        java.util.List<KhachHang> usersWithNullEmail = khachHangRepository.findUsersWithNullEmail();
        if (!usersWithNullEmail.isEmpty()) {
            // If there's a user with NULL email, update it with our email
            KhachHang userToUpdate = usersWithNullEmail.get(0);
            log.info("Found user with NULL email, updating with email: {}", email);
            userToUpdate.setEmail(email);
            if (userToUpdate.getTenTaiKhoan() == null || userToUpdate.getTenTaiKhoan().isBlank()) {
                userToUpdate.setTenTaiKhoan(email);
            }
            if (userInfo.getName() != null && !userInfo.getName().isBlank()) {
                userToUpdate.setTenKhachHang(userInfo.getName());
            }
            userToUpdate.setTrangThai(true);
            return khachHangRepository.save(userToUpdate);
        }
        
        java.util.List<KhachHang> usersWithNullUsername = khachHangRepository.findUsersWithNullTenTaiKhoan();
        if (!usersWithNullUsername.isEmpty()) {
            // If there's a user with NULL ten_tai_khoan, update it
            KhachHang userToUpdate = usersWithNullUsername.get(0);
            log.info("Found user with NULL ten_tai_khoan, updating with email: {}", email);
            if (userToUpdate.getEmail() == null || userToUpdate.getEmail().isBlank()) {
                userToUpdate.setEmail(email);
            }
            userToUpdate.setTenTaiKhoan(email);
            if (userInfo.getName() != null && !userInfo.getName().isBlank()) {
                userToUpdate.setTenKhachHang(userInfo.getName());
            }
            userToUpdate.setTrangThai(true);
            return khachHangRepository.save(userToUpdate);
        }

        // Create new user - ensure both email and ten_tai_khoan are set and never null
        // Also ensure so_dien_thoai is set to avoid unique constraint on NULL
        KhachHang newUser = new KhachHang();
        newUser.setEmail(email); // Never null
        String displayName = userInfo.getName() != null && !userInfo.getName().isBlank() 
            ? userInfo.getName() 
            : email.split("@")[0];
        newUser.setTenKhachHang(displayName);
        newUser.setTenTaiKhoan(email); // Never null, same as email to ensure uniqueness
        newUser.setMatKhau(null); // Password can be null for OAuth users
        // Set so_dien_thoai to a unique value to avoid NULL unique constraint violation
        // Column has length limit of 12, so use shortened format: o{providerId} (max 11 chars)
        String providerId = userInfo.getProviderId();
        // Truncate provider ID if needed to fit in 12 characters (format: "o" + providerId, max 11 chars)
        String phoneValue = "o" + (providerId.length() > 11 ? providerId.substring(0, 11) : providerId);
        newUser.setSoDienThoai(phoneValue);
        newUser.setTrangThai(true);
        newUser.setDeleted(false);
        newUser.setPhanLoai(0);
        // Set create_at to current date
        newUser.setCreateAt(java.time.LocalDate.now());
        newUser.setCreateBy(1); // System user

        // Retry logic to handle race conditions
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Creating new OAuth user with email: {} (attempt {}/{})", email, attempt, maxRetries);
                
                // Double-check before saving (in case another thread created the user)
                existing = khachHangRepository.findByEmail(email);
                if (existing != null) {
                    log.info("User found by email during retry attempt {}", attempt);
                    return existing;
                }
                
                existingByUsername = khachHangRepository.findByTenTaiKhoan(email);
                if (existingByUsername != null) {
                    log.info("User found by username during retry attempt {}", attempt);
                    if (existingByUsername.getEmail() == null || existingByUsername.getEmail().isBlank()) {
                        existingByUsername.setEmail(email);
                        khachHangRepository.save(existingByUsername);
                    }
                    return existingByUsername;
                }
                
                return khachHangRepository.save(newUser);
            } catch (org.hibernate.exception.ConstraintViolationException e) {
                log.warn("Unique constraint violation on attempt {}: {}", attempt, e.getMessage());
                
                if (attempt < maxRetries) {
                    // Wait a bit before retrying (exponential backoff)
                    try {
                        Thread.sleep(100 * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                    
                    // Try to find existing user before retrying
                    existing = khachHangRepository.findByEmail(email);
                    if (existing != null) {
                        log.info("Found existing user by email after constraint violation (attempt {})", attempt);
                        return existing;
                    }
                    
                    existingByUsername = khachHangRepository.findByTenTaiKhoan(email);
                    if (existingByUsername != null) {
                        log.info("Found existing user by username after constraint violation (attempt {})", attempt);
                        if (existingByUsername.getEmail() == null || existingByUsername.getEmail().isBlank()) {
                            existingByUsername.setEmail(email);
                            khachHangRepository.save(existingByUsername);
                        }
                        return existingByUsername;
                    }
                    
                    // Continue to next retry attempt
                    continue;
                } else {
                    // Last attempt failed, try one more time to find existing user
                    log.error("All retry attempts failed. Final attempt to find existing user.");
                    existing = khachHangRepository.findByEmail(email);
                    if (existing != null) {
                        return existing;
                    }
                    existingByUsername = khachHangRepository.findByTenTaiKhoan(email);
                    if (existingByUsername != null) {
                        if (existingByUsername.getEmail() == null || existingByUsername.getEmail().isBlank()) {
                            existingByUsername.setEmail(email);
                            khachHangRepository.save(existingByUsername);
                        }
                        return existingByUsername;
                    }
                    
                    // If we still can't find the user, log error and re-throw
                    log.error("Constraint violation after all retries. Email: {}", email);
                    throw new RuntimeException("Failed to create or find OAuth user after " + maxRetries + " attempts: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                if (e instanceof org.hibernate.exception.ConstraintViolationException) {
                    // Re-throw to be handled by the outer catch
                    throw e;
                }
                log.error("Unexpected error creating OAuth user (attempt {}): {}", attempt, e.getMessage(), e);
                throw e;
            }
        }
        
        // Should never reach here, but just in case
        throw new RuntimeException("Failed to create OAuth user after " + maxRetries + " attempts");
    }

    public static class OAuthUserInfo {
        private final String email;
        private final String name;
        private final String providerId;
        private final String provider;

        public OAuthUserInfo(String email, String name, String providerId, String provider) {
            this.email = email;
            this.name = name;
            this.providerId = providerId;
            this.provider = provider;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public String getProviderId() {
            return providerId;
        }

        public String getProvider() {
            return provider;
        }
    }
}

