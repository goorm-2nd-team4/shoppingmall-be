package com.goorm.shoppingmall.auth.controller;

import com.goorm.shoppingmall.auth.dto.LoginRequest;
import com.goorm.shoppingmall.auth.dto.LoginResult;
import com.goorm.shoppingmall.auth.dto.RegisterRequest;
import com.goorm.shoppingmall.auth.dto.RegisterResult;
import com.goorm.shoppingmall.auth.service.AuthService;
import com.goorm.shoppingmall.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResult> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.of("회원가입 성공", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.of("로그인 성공", authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.message("로그아웃 성공");
    }
}
