package com.peredereevin.authservice.controller;

import com.peredereevin.authservice.entity.Role;
import com.peredereevin.authservice.io.AuthRequest;
import com.peredereevin.authservice.io.AuthResponse;
import com.peredereevin.authservice.io.ResetPasswordRequest;
import com.peredereevin.authservice.service.AppUserDetailsService;
import com.peredereevin.authservice.service.ProfileService;
import com.peredereevin.authservice.service.RefreshTokenService;
import com.peredereevin.authservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticate(request.getEmail(), request.getPassword());
            final UserDetails userDetails = appUserDetailsService.loadUserByUsername(request.getEmail());
            final String email = userDetails.getUsername();

            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst().orElse("ROLE_USER");
            if (role.startsWith("ROLE_")) role = role.substring(5);
            final String accessToken = jwtUtil.generateToken(email, Role.valueOf(role));


            // Refresh token (сохраняется в БД)
            final String refreshToken = refreshTokenService.createRefreshToken(email);

            // Устанавливаем access token в httpOnly куку
            ResponseCookie cookie = ResponseCookie.from("jwt", accessToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))  // или срок жизни access?
                    .sameSite("Strict")
                    .build();

            AuthResponse responseBody = AuthResponse.builder()
                    .email(email)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(responseBody);
        } catch (BadCredentialsException exception) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message","Логин или пароль введены неправильно!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (DisabledException exception) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message","Аккаунт отключен!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception exception) {
            exception.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Ошибка аутентификации!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refreshToken обязателен"));
        }

        return refreshTokenService.findByToken(refreshToken)
                .filter(refreshTokenService::isValid)
                .map(token -> {
                    String email = token.getEmail();
                    String newAccessToken = jwtUtil.generateToken(email);
                    ResponseCookie cookie = ResponseCookie.from("jwt", newAccessToken)
                            .httpOnly(true)
                            .path("/")
                            .maxAge(Duration.ofDays(1))
                            .sameSite("Strict")
                            .build();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body(Map.of("accessToken", newAccessToken));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Невалидный или истекший refresh-токен")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        if (email != null) {
            refreshTokenService.deleteByEmail(email);
        }
        // Удаляем куку
        ResponseCookie deleteCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(Map.of("message", "Вы вышли из системы"));
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        return ResponseEntity.ok(email != null);
    }

    @PostMapping("/send-reset-otp")
    public void sendResetOtp(@RequestParam String email) {
        try {
            profileService.sendResetOtp(email);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping("/reset")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            profileService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping("/send-otp")
    public void sendVerifyOtp(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        try {
            profileService.sendOtp(email);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public void verifyEmail(@RequestBody Map<String, Object> request,
                            @CurrentSecurityContext(expression = "authentication?.name") String email) {
        if (request.get("otp").toString() == null || request.get("otp").toString().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Отсутствуют детали");
        }

        try {
            profileService.verifyOtp(email, request.get("otp").toString());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }
}