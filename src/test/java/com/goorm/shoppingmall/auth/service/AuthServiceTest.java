package com.goorm.shoppingmall.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.goorm.shoppingmall.domain.auth.dto.LoginRequest;
import com.goorm.shoppingmall.domain.auth.dto.LoginResult;
import com.goorm.shoppingmall.domain.auth.dto.RegisterRequest;
import com.goorm.shoppingmall.domain.auth.dto.RegisterResult;
import com.goorm.shoppingmall.domain.auth.service.AuthService;
import com.goorm.shoppingmall.global.exception.CustomException;
import com.goorm.shoppingmall.global.exception.ErrorCode;
import com.goorm.shoppingmall.global.jwt.JwtProvider;
import com.goorm.shoppingmall.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    void register() {
        RegisterResult response = authService.register(
                new RegisterRequest("user1@example.com", "password123", "password123", "user1")
        );

        assertThat(response.user_email()).isEqualTo("user1@example.com");
        assertThat(response.user_name()).isEqualTo("user1");
        assertThat(response.user_role().name()).isEqualTo("USER");
        assertThat(userRepository.findByEmail("user1@example.com")).isPresent();
    }

    @Test
    void login() {
        authService.register(new RegisterRequest("login@example.com", "password123", "password123", "tester"));

        LoginResult response = authService.login(new LoginRequest("login@example.com", "password123"));

        assertThat(response.user_email()).isEqualTo("login@example.com");
        assertThat(response.user_name()).isEqualTo("tester");
        assertThat(response.token()).isNotBlank();
        assertThat(jwtProvider.validateToken(response.token())).isTrue();
        assertThat(jwtProvider.getEmail(response.token())).isEqualTo("login@example.com");
        assertThat(jwtProvider.getRole(response.token())).isEqualTo("USER");
    }

    @Test
    void rejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("goorm@example.com", "password123", "password123", "tester");
        authService.register(request);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode())
                            .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
                });
    }

    @Test
    void rejectInvalidPassword() {
        authService.register(new RegisterRequest("wrongpw@example.com", "password123", "password123", "tester"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("wrongpw@example.com", "password456")))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode())
                            .isEqualTo(ErrorCode.AUTH_INVALID_CREDENTIALS);
                });
    }

    @Test
    void storeEncodedPassword() {
        authService.register(new RegisterRequest("encoded@example.com", "password123", "password123", "tester"));

        String encodedPassword = userRepository.findByEmail("encoded@example.com")
                .orElseThrow()
                .getPassword();

        assertThat(encodedPassword).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", encodedPassword)).isTrue();
    }

    @Test
    void rejectMismatchedPasswordConfirmation() {
        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("mismatch@example.com", "password123", "password456", "tester")
        ))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode())
                            .isEqualTo(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
                });
    }
}