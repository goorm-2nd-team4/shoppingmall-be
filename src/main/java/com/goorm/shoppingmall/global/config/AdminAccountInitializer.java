package com.goorm.shoppingmall.global.config;

import com.goorm.shoppingmall.domain.user.domain.User;
import com.goorm.shoppingmall.domain.user.domain.UserRole;
import com.goorm.shoppingmall.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminAccountProperties adminAccountProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String encodedPassword = passwordEncoder.encode(adminAccountProperties.password());

        userRepository.findByEmail(adminAccountProperties.email())
                .ifPresentOrElse(
                        user -> user.updateProfile(encodedPassword, adminAccountProperties.name(), UserRole.ADMIN),
                        () -> userRepository.save(User.createAdmin(
                                adminAccountProperties.email(),
                                encodedPassword,
                                adminAccountProperties.name()
                        ))
                );
    }
}
