package com.goorm.shoppingmall.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.goorm.shoppingmall.auth.dto.LoginRequest;
import com.goorm.shoppingmall.auth.dto.LoginResult;
import com.goorm.shoppingmall.auth.dto.RegisterRequest;
import com.goorm.shoppingmall.auth.dto.RegisterResult;
import com.goorm.shoppingmall.global.error.DuplicateResourceException;
import com.goorm.shoppingmall.global.error.InvalidCredentialsException;
import com.goorm.shoppingmall.global.error.InvalidRequestException;
import com.goorm.shoppingmall.user.repository.UserRepository;
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
    }

    @Test
    void rejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("goorm@example.com", "password123", "password123", "tester");
        authService.register(request);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    void rejectInvalidPassword() {
        authService.register(new RegisterRequest("wrongpw@example.com", "password123", "password123", "tester"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("wrongpw@example.com", "password456")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
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
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("비밀번호 확인이 일치하지 않습니다.");
    }
}
