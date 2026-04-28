package com.goorm.shoppingmall.domain.user.service;

import com.goorm.shoppingmall.domain.user.dto.UserResponse;
import com.goorm.shoppingmall.domain.user.repository.UserRepository;
import com.goorm.shoppingmall.global.exception.CustomException;
import com.goorm.shoppingmall.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getMe(String email) {
        return UserResponse.from(
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND))
        );
    }
}