package com.goorm.shoppingmall.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Email
        @Size(max = 100)
        String user_email,

        @NotBlank
        @Size(min = 8, max = 20)
        String user_password
) {
}
