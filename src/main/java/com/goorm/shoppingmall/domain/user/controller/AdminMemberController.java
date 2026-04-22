package com.goorm.shoppingmall.domain.user.controller;

import com.goorm.shoppingmall.global.response.ApiResponse;
import com.goorm.shoppingmall.domain.user.dto.MemberListResponse;
import com.goorm.shoppingmall.domain.user.dto.MemberResponse;
import com.goorm.shoppingmall.domain.user.dto.MemberRoleUpdateRequest;
import com.goorm.shoppingmall.domain.user.service.AdminMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    @GetMapping
    public ApiResponse<MemberListResponse> getMembers() {
        return ApiResponse.of("회원 목록 조회 성공", adminMemberService.getMembers());
    }

    @GetMapping("/{memberId}")
    public ApiResponse<MemberResponse> getMember(@PathVariable Long memberId) {
        return ApiResponse.of("회원 조회 성공", adminMemberService.getMember(memberId));
    }

    @PatchMapping("/{memberId}/role")
    public ApiResponse<MemberResponse> updateRole(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberRoleUpdateRequest request
    ) {
        return ApiResponse.of("회원 권한 변경 성공", adminMemberService.updateRole(memberId, request.role()));
    }

    @DeleteMapping("/{memberId}")
    public ApiResponse<Void> deleteMember(@PathVariable Long memberId) {
        adminMemberService.deleteMember(memberId);
        return ApiResponse.message("회원 삭제 성공");
    }
}
