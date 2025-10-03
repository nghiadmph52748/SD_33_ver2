package org.example.be_sp.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    private static final String SECRET = "mySecretKeymySecretKeymySecretKeymySecretKeymySecretKey"; // Should be from config
    private static final int JWT_EXPIRATION = 3600000; // 1 hour in milliseconds
    private static final int JWT_REFRESH_EXPIRATION = 604800000; // 7 days in milliseconds

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, username, JWT_REFRESH_EXPIRATION);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return createToken(claims, subject, JWT_EXPIRATION);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }

    /**
     * Reset thời hạn của token về 1 giờ nếu token vẫn còn hạn
     *
     * @param token Token hiện tại
     * @return Token mới với thời hạn 1 giờ hoặc null nếu token đã hết hạn
     */
    public String refreshTokenIfValid(String token) {
        try {
            if (!isTokenExpired(token)) {
                String username = getUsernameFromToken(token);
                // Tạo token mới với thời hạn 1 giờ
                return generateToken(username);
            }
            return null; // Token đã hết hạn
        } catch (Exception e) {
            return null; // Token không hợp lệ
        }
    }

    /**
     * Kiểm tra token và trả về token mới nếu cần refresh
     *
     * @param token Token hiện tại
     * @param username Username để validate
     * @return Token mới nếu cần refresh, token cũ nếu vẫn còn thời gian, null
     * nếu hết hạn
     */
    public String validateAndRefreshToken(String token, String username) {
        try {
            if (validateToken(token, username)) {
                // Kiểm tra thời gian còn lại của token
                Date expiration = getExpirationDateFromToken(token);
                Date now = new Date();
                long timeLeft = expiration.getTime() - now.getTime();

                // Nếu token còn ít hơn 30 phút, refresh token
                if (timeLeft < 1800000) { // 30 minutes in milliseconds
                    return generateToken(username);
                }

                // Token vẫn còn thời gian, trả về token cũ
                return token;
            }
            return null; // Token không hợp lệ hoặc đã hết hạn
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String type = claims.get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }
}
