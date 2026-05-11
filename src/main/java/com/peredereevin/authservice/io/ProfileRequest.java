package com.peredereevin.authservice.io;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileRequest {

    @NotBlank(message = "Поле name не должно быть пустым")
    private String name;
    @Email(message = "Введите валидный адрес электронной почты")
    @NotNull(message = "Поле email не должно быть пустым")
    private String email;
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов!")
    private String password;
}