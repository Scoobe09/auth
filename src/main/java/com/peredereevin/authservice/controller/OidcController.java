package com.peredereevin.authservice.controller;

import com.peredereevin.authservice.security.KeyPairProvider;
import com.peredereevin.authservice.service.AppUserDetailsService;
import com.peredereevin.authservice.service.ProfileService;
import com.peredereevin.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OidcController {

    private final KeyPairProvider keyPairProvider;
    private final JwtUtil jwtUtil;
    private final AppUserDetailsService appUserDetailsService;
    private final ProfileService profileService;

    /**
     * JWKS endpoint — возвращает публичный ключ в формате JSON Web Key Set.
     * Стандарт: https://tools.ietf.org/html/rfc7517
     */
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        RSAPublicKey publicKey = keyPairProvider.getPublicKey();
        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("n", base64UrlEncode(publicKey.getModulus().toByteArray()));
        jwk.put("e", base64UrlEncode(publicKey.getPublicExponent().toByteArray()));
        jwk.put("alg", "RS256");
        jwk.put("use", "sig");
        jwk.put("kid", generateKeyId(publicKey));

        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", List.of(jwk));
        return ResponseEntity.ok(jwks);
    }

    /**
     * OIDC UserInfo endpoint — возвращает данные текущего пользователя по access-токену.
     * Требуется аутентификация.
     */
    @GetMapping("/userinfo")
    public ResponseEntity<?> userInfo(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = userDetails.getUsername();
        // Здесь можно добавить загрузку из БД для получения дополнительных полей
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", email);
        claims.put("email", email);
        claims.put("email_verified", true); // предположим, что верификация OTP подтверждает
        claims.put("preferred_username", email);
        // можно добавить name, picture и т.д.
        return ResponseEntity.ok(claims);
    }

    /**
     * Token Introspection (RFC 7662).
     * Проверяет активность токена. Принимает application/x-www-form-urlencoded.
     */
    @PostMapping("/introspect")
    public ResponseEntity<Map<String, Object>> introspect(@RequestParam("token") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = jwtUtil.extractUsername(token);
            if (email != null && jwtUtil.isTokenValid(token, email)) {
                response.put("active", true);
                response.put("sub", email);
                response.put("email", email);
                // дополнительные поля по желанию
            } else {
                response.put("active", false);
            }
        } catch (Exception e) {
            response.put("active", false);
        }
        return ResponseEntity.ok(response);
    }

    // --- Вспомогательные методы ---
    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private String generateKeyId(RSAPublicKey publicKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(publicKey.getEncoded());
            return base64UrlEncode(hash);
        } catch (NoSuchAlgorithmException e) {
            return "default-kid";
        }
    }
}