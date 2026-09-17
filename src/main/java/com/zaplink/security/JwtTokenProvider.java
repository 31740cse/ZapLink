package com.zaplink.security;

import com.zaplink.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final AppProperties appProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = appProperties.getJwtSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token for user (0.11.5 syntax)
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getJwtExpirationMs());

        return Jwts.builder()
                .setSubject(String.valueOf(userId))              // ✓ Use setSubject (not subject)
                .claim("username", username)
                .setIssuedAt(now)                                // ✓ Use setIssuedAt (not issuedAt)
                .setExpiration(expiryDate)                       // ✓ Use setExpiration (not expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Get Claims from token
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error parsing token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                return Long.parseLong(claims.getSubject());
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract username from token
     */
    public String extractUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                return claims.get("username", String.class);
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}