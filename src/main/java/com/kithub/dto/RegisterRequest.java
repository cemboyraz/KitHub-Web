package com.kithub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Kullanıcı adı boş bırakılamaz!")
        String username,

        @NotBlank(message = "Email boş bırakılamaz!")
        @Email(message = "Lütfen geçerli bir email adresi giriniz!")
        String email,

        @NotBlank(message = "Şifre boş bırakılamaz!")
        @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır!")
        String password
) { }