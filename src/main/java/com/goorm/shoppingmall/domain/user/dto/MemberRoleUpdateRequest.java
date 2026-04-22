package com.goorm.shoppingmall.domain.user.dto;

import com.goorm.shoppingmall.domain.user.domain.UserRole;
import jakarta.validation.constraints.NotNull;

public record MemberRoleUpdateRequest(
        @NotNull(message = "변경할 권한은 필수입니다.")
        UserRole role
) {
}
