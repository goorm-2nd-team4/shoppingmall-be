package com.goorm.shoppingmall.user.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goorm.shoppingmall.global.jwt.JwtProvider;
import com.goorm.shoppingmall.domain.user.domain.User;
import com.goorm.shoppingmall.domain.user.domain.UserRole;
import com.goorm.shoppingmall.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AdminMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private Long userId;

    @BeforeEach
    void setUp() {
        userRepository.findByEmail("member@test.com")
                .ifPresent(userRepository::delete);

        User user = userRepository.save(User.create(
                "member@test.com",
                passwordEncoder.encode("password123"),
                "일반 회원"
        ));
        userId = user.getId();
    }

    @Test
    void seedAdminExists() {
        User admin = userRepository.findByEmail("admin@test.com")
                .orElseThrow();

        org.assertj.core.api.Assertions.assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void rejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/admin/members"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("인증이 필요합니다.")));
    }

    @Test
    void rejectNonAdminRequest() throws Exception {
        mockMvc.perform(get("/api/admin/members")
                        .header("Authorization", bearerToken("member@test.com", UserRole.USER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("접근 권한이 없습니다.")));
    }

    @Test
    void allowAdminToGetMembers() throws Exception {
        mockMvc.perform(get("/api/admin/members")
                        .header("Authorization", bearerToken("admin@test.com", UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("회원 목록 조회 성공")))
                .andExpect(jsonPath("$.data.totalCount", is(2)))
                .andExpect(jsonPath("$.data.adminCount", is(1)))
                .andExpect(jsonPath("$.data.members", hasSize(2)));
    }

    @Test
    void allowAdminToUpdateMemberRole() throws Exception {
        mockMvc.perform(patch("/api/admin/members/{memberId}/role", userId)
                        .header("Authorization", bearerToken("admin@test.com", UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("회원 권한 변경 성공")))
                .andExpect(jsonPath("$.data.role", is("ADMIN")));
    }

    @Test
    void allowAdminToDeleteMember() throws Exception {
        mockMvc.perform(delete("/api/admin/members/{memberId}", userId)
                        .header("Authorization", bearerToken("admin@test.com", UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("회원 삭제 성공")));

        org.assertj.core.api.Assertions.assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    void rejectDeletingSeedAdmin() throws Exception {
        Long adminId = userRepository.findByEmail("admin@test.com")
                .orElseThrow()
                .getId();

        mockMvc.perform(delete("/api/admin/members/{memberId}", adminId)
                        .header("Authorization", bearerToken("admin@test.com", UserRole.ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("기본 관리자 계정은 삭제할 수 없습니다.")));
    }

    private String bearerToken(String email, UserRole role) {
        return "Bearer " + jwtProvider.generateToken(email, role.name());
    }
}
