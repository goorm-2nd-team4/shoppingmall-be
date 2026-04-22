package com.goorm.shoppingmall.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

public record RegisterRequest(
        @NotBlank
        @Email
        @Size(max = 100)
        String user_email,

        @NotBlank
        @Size(min = 8, max = 20)
        String user_password,

        @NotBlank
        @Size(min = 8, max = 20)
        String user_password_confirm,

        @NotBlank
        @Size(max = 50)
        String user_name
) {

    @AssertTrue(message = "비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        return user_password != null && user_password.equals(user_password_confirm);
    }
}
