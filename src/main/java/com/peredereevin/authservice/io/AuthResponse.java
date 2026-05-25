package com.peredereevin.authservice.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String email;
    private String accessToken;
    private String refreshToken;
}