package com.goorm.shoppingmall.auth.service;

import com.goorm.shoppingmall.auth.dto.LoginRequest;
import com.goorm.shoppingmall.auth.dto.LoginResult;
import com.goorm.shoppingmall.auth.dto.RegisterRequest;
import com.goorm.shoppingmall.auth.dto.RegisterResult;
import com.goorm.shoppingmall.global.error.DuplicateResourceException;
import com.goorm.shoppingmall.global.error.InvalidCredentialsException;
import com.goorm.shoppingmall.global.error.InvalidRequestException;
import com.goorm.shoppingmall.user.domain.User;
import com.goorm.shoppingmall.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResult register(RegisterRequest request) {
        if (!request.isPasswordConfirmed()) {
            throw new InvalidRequestException("비밀번호 확인이 일치하지 않습니다.");
        }

        if (userRepository.existsByEmail(request.user_email())) {
            throw new DuplicateResourceException("이미 사용 중인 이메일입니다.");
        }

        User user = User.create(
                request.user_email(),
                passwordEncoder.encode(request.user_password()),
                request.user_name()
        );

        return RegisterResult.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.user_email())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.user_password(), user.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return LoginResult.from(user, UUID.randomUUID().toString());
    }
}
