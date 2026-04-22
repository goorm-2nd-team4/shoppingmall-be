package com.goorm.shoppingmall.domain.auth.dto;

import com.goorm.shoppingmall.domain.user.domain.User;
import com.goorm.shoppingmall.domain.user.domain.UserRole;

public record LoginResult(
        Long id,
        String user_email,
        String user_name,
        UserRole user_role,
        String token
) {
    public static LoginResult from(User user, String token) {
        return new LoginResult(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                token
        );
    }
}
