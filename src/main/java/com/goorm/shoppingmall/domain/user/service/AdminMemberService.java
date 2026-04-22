package com.goorm.shoppingmall.domain.user.service;

import com.goorm.shoppingmall.global.config.AdminAccountProperties;
import com.goorm.shoppingmall.global.error.InvalidRequestException;
import com.goorm.shoppingmall.global.error.ResourceNotFoundException;
import com.goorm.shoppingmall.domain.user.domain.User;
import com.goorm.shoppingmall.domain.user.domain.UserRole;
import com.goorm.shoppingmall.domain.user.dto.MemberListResponse;
import com.goorm.shoppingmall.domain.user.dto.MemberResponse;
import com.goorm.shoppingmall.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final UserRepository userRepository;
    private final AdminAccountProperties adminAccountProperties;

    @Transactional(readOnly = true)
    public MemberListResponse getMembers() {
        List<MemberResponse> members = userRepository.findAllByOrderByIdAsc().stream()
                .map(MemberResponse::from)
                .toList();

        return MemberListResponse.of(
                members.size(),
                userRepository.countByRole(UserRole.ADMIN),
                members
        );
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(Long memberId) {
        return MemberResponse.from(findUser(memberId));
    }

    @Transactional
    public MemberResponse updateRole(Long memberId, UserRole role) {
        User user = findUser(memberId);

        if (isSeedAdmin(user) && role != UserRole.ADMIN) {
            throw new InvalidRequestException("기본 관리자 계정의 ADMIN 권한은 제거할 수 없습니다.");
        }

        user.updateRole(role);
        return MemberResponse.from(user);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        User user = findUser(memberId);

        if (isSeedAdmin(user)) {
            throw new InvalidRequestException("기본 관리자 계정은 삭제할 수 없습니다.");
        }

        userRepository.delete(user);
    }

    private User findUser(Long memberId) {
        return userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다."));
    }

    private boolean isSeedAdmin(User user) {
        return user.getEmail().equals(adminAccountProperties.email());
    }
}
