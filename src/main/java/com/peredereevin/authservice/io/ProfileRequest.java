package com.peredereevin.authservice.io;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Min(6)
    private String password;
}