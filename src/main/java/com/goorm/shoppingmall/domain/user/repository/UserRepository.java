package com.goorm.shoppingmall.domain.user.repository;

import com.goorm.shoppingmall.domain.user.domain.User;
import com.goorm.shoppingmall.domain.user.domain.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findAllByOrderByIdAsc();

    long countByRole(UserRole role);
}
