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
    // поле password
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Пароль должен быть не менее 8 символов, содержать цифру, строчную и заглавную букву, а также спецсимвол (@#$%^&+=!)"
    )
    private String password;
}