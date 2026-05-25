package com.peredereevin.authservice.util;

import com.peredereevin.authservice.config.JwtProperties;
import com.peredereevin.authservice.security.KeyPairProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private final KeyPairProvider keyPairProvider;
    private final long accessTokenExpirationMs;

    public JwtUtil(KeyPairProvider keyPairProvider, JwtProperties jwtProperties) {
        this.keyPairProvider = keyPairProvider;
        this.accessTokenExpirationMs = jwtProperties.getAccessTokenExpiration();
    }

    // ---------- Генерация токена (публичный метод, сохраняем старую сигнатуру) ----------
    public String generateToken(String email) {
        return generateToken(new HashMap<>(), email);
    }

    public String generateToken(Map<String, Object> extraClaims, String email) {
        log.debug("Generating JWT for subject: {}", email);
        RSAPrivateKey privateKey = keyPairProvider.getPrivateKey();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(privateKey)               // RS256
                .compact();
    }

    // ---------- Валидация ----------
    public boolean isTokenValid(String token, String userEmail) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userEmail)) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ---------- Извлечение данных ----------
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ---------- Вспомогательные методы ----------
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        RSAPublicKey publicKey = keyPairProvider.getPublicKey();
        return Jwts.parser()
                .verifyWith(publicKey)              // асимметричная проверка
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}