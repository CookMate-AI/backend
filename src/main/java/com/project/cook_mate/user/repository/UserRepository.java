package com.project.cook_mate.user.repository;

import com.project.cook_mate.user.dto.UserDto;
import com.project.cook_mate.user.dto.UserResponseDto;
import com.project.cook_mate.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    boolean existsByNickName(String nickName);

    User findByuserId(String userId);

    @Query("SELECT NEW com.project.cook_mate.user.dto.UserDto(u.userId, u.userPw, u.email) FROM User u WHERE u.email = :email")
    Optional<UserDto> findUserDtoByUserEmail(@Param("email") String email);

    Optional<User> findByuserIdAndEmail(@Param("email") String email, @Param("userId") String userId);

    @Query("SELECT NEW com.project.cook_mate.user.dto.UserResponseDto(u.userId, u.nickName, u.email, u.joinDate, u.updateDate, u.role) FROM User u WHERE u.userId = :userId AND u.secession = :secession")
    Optional<UserResponseDto> findUserByUserId(@Param("userId") String userId, @Param("secession") int secession);
}
