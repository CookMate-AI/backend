package com.project.cook_mate.user.repository;

import com.project.cook_mate.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    boolean existsByNickName(String nickName);

    User findByuserId(String userId);
}
