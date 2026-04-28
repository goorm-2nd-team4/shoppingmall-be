package com.goorm.shoppingmall.domain.user.dto;

import java.util.List;

public record MemberListResponse(
        long totalCount,
        long adminCount,
        List<MemberResponse> members
) {
    public static MemberListResponse of(long totalCount, long adminCount, List<MemberResponse> members) {
        return new MemberListResponse(totalCount, adminCount, members);
    }
}
