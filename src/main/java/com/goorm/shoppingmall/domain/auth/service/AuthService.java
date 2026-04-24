package com.goorm.shoppingmall.domain.auth.service;

import com.goorm.shoppingmall.domain.auth.dto.LoginRequest;
import com.goorm.shoppingmall.domain.auth.dto.LoginResult;
import com.goorm.shoppingmall.domain.auth.dto.RegisterRequest;
import com.goorm.shoppingmall.domain.auth.dto.RegisterResult;
import com.goorm.shoppingmall.global.exception.CustomException;
import com.goorm.shoppingmall.global.exception.ErrorCode;
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
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        if (userRepository.existsByEmail(request.user_email())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
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
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.user_password(), user.getPassword())) {
            throw new CustomException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        String token = jwtProvider.generateToken(user.getEmail(), user.getRole().name());
        return LoginResult.from(user, token);
    }
}