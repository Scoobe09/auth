package com.peredereevin.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@ Entity
@ Table(name = "users")
@ Data
@ Builder
@ AllArgsConstructor
@ NoArgsConstructor
public class User {
    @ Id
    @ GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ Column(unique = true)
    private String userId;

    private String name;

    @ Column(unique = true)
    private String email;

    private String password;

    private String verifyOtp;
    private Boolean isAccountVerified;
    private Long verifyOtpExpireAt;

    private String resetOtp;
    private Long resetOtpExpireAt;

    // Tech-2: ограничение частоты OTP-запросов
    private Long lastOtpRequestTime;       // время последнего запроса verify-OTP (мс)
    private Long lastResetOtpRequestTime;  // время последнего запроса reset-OTP (мс)

    // Роль пользователя (добавлено на этапе RBAC, теперь явно в сущности)
    @ Enumerated(EnumType.STRING)
    @ Builder.Default
    private Role role = Role.USER;

    @ CreationTimestamp
    @ Column(updatable = false)
    private Timestamp createdAt;

    @ UpdateTimestamp
    private Timestamp updatedAt;
}