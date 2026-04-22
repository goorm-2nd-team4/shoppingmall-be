package com.goorm.shoppingmall.domain.auth.service;

import com.goorm.shoppingmall.domain.auth.dto.LoginRequest;
import com.goorm.shoppingmall.domain.auth.dto.LoginResult;
import com.goorm.shoppingmall.domain.auth.dto.RegisterRequest;
import com.goorm.shoppingmall.domain.auth.dto.RegisterResult;
import com.goorm.shoppingmall.global.error.DuplicateResourceException;
import com.goorm.shoppingmall.global.error.InvalidCredentialsException;
import com.goorm.shoppingmall.global.error.InvalidRequestException;
import com.goorm.shoppingmall.global.jwt.JwtProvider;
import com.goorm.shoppingmall.domain.user.domain.User;
import com.goorm.shoppingmall.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

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

        String token = jwtProvider.generateToken(user.getEmail(), user.getRole().name());
        return LoginResult.from(user, token);
    }
}
