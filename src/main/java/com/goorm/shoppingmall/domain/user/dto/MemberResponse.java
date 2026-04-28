package com.goorm.shoppingmall.domain.user.dto;

import com.goorm.shoppingmall.domain.user.domain.User;
import com.goorm.shoppingmall.domain.user.domain.UserRole;

public record MemberResponse(
        Long id,
        String email,
        String name,
        UserRole role
) {
    public static MemberResponse from(User user) {
        return new MemberResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }
}
