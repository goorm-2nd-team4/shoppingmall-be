package com.goorm.shoppingmall.domain.user.service;

import com.goorm.shoppingmall.global.config.AdminAccountProperties;
import com.goorm.shoppingmall.global.exception.CustomException;
import com.goorm.shoppingmall.global.exception.ErrorCode;
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
            throw new CustomException(ErrorCode.SEED_ADMIN_ROLE_CHANGE_NOT_ALLOWED);
        }

        user.updateRole(role);
        return MemberResponse.from(user);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        User user = findUser(memberId);

        if (isSeedAdmin(user)) {
            throw new CustomException(ErrorCode.SEED_ADMIN_DELETE_NOT_ALLOWED);
        }

        userRepository.delete(user);
    }

    private User findUser(Long memberId) {
        return userRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private boolean isSeedAdmin(User user) {
        return user.getEmail().equals(adminAccountProperties.email());
    }
}