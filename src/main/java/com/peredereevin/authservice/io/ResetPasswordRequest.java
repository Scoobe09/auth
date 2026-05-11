package com.peredereevin.authservice.io;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "Введите новый пароль")
    private String newPassword;
    @NotBlank(message = "Введите одноразовый код")
    private String otp;
    @NotBlank(message = "Введите email")
    private String email;
}
