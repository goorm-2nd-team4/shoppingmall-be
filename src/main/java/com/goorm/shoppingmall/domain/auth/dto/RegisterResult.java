package com.goorm.shoppingmall.domain.auth.dto;

import com.goorm.shoppingmall.domain.user.domain.User;
import com.goorm.shoppingmall.domain.user.domain.UserRole;

public record RegisterResult(
        Long id,
        String user_email,
        String user_name,
        UserRole user_role
) {
    public static RegisterResult from(User user) {
        return new RegisterResult(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }
}
