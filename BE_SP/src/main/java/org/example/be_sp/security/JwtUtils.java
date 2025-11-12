package org.example.be_sp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private static final String SECRET_KEY = "your_secret_key_which_should_be_long_enough_to_be_secure_1234567890";
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; // 1 giờ
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 ngày

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ✅ Hàm chuẩn hoá role (xoá dấu tiếng Việt + thêm tiền tố ROLE_)
    private String normalizeRole(String role) {
        if (role == null) return "ROLE_USER";

        // Bỏ dấu tiếng Việt
        String normalized = Normalizer.normalize(role, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", ""); // xóa dấu

        // Viết hoa và thay khoảng trắng thành gạch dưới
        normalized = normalized.toUpperCase().replaceAll("\\s+", "_");

        // Thêm tiền tố ROLE_ nếu chưa có
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return normalized;
    }

    // ✅ Sinh Access Token có roles
    public String generateToken(String username, List<String> roles) {
        // Chuẩn hoá danh sách roles
        List<String> normalizedRoles = roles.stream()
                .map(this::normalizeRole)
                .collect(Collectors.toList());

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", normalizedRoles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Sinh Refresh Token
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseToken(token).getBody().getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        return parseToken(token).getBody().getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    public boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    public List<String> getRolesFromToken(String token) {
        Object roles = parseToken(token).getBody().get("roles");
        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream().map(Object::toString).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }

    // ✅ Tự động refresh token nếu sắp hết hạn (còn < 30 phút)
    public String validateAndRefreshToken(String token, String username) {
        if (!validateToken(token, username)) return null;

        Date expiration = getExpirationDateFromToken(token);
        long timeLeft = expiration.getTime() - System.currentTimeMillis();

        if (timeLeft < 30 * 60 * 1000) { // còn dưới 30 phút
            List<String> roles = getRolesFromToken(token);
            return generateToken(username, roles);
        }
        return token;
    }
}
