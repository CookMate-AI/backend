package com.project.cook_mate.user.repository;

import com.project.cook_mate.user.dto.UserDto;
import com.project.cook_mate.user.dto.UserResponseDto;
import com.project.cook_mate.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUserIdAndSecession(String userId, int secession);

    boolean existsByEmailAndSecession(String email, int secession);

    boolean existsByNickNameAndSecession(String nickName, int secession);

    Optional<User> findByUserIdAndSecession(String userId, int secession);

    @Query("SELECT NEW com.project.cook_mate.user.dto.UserDto(u.userId, u.userPw, u.email) FROM User u WHERE u.email = :email AND u.secession = :secession")
    Optional<UserDto> findUserDtoByUserEmail(@Param("email") String email, @Param("secession") int secession);

    Optional<User> findByUserIdAndEmailAndSecession(@Param("email") String email, @Param("userId") String userId, int secession);

    @Query("SELECT NEW com.project.cook_mate.user.dto.UserResponseDto(u.userId, u.nickName, u.email, u.joinDate, u.updateDate, u.role) FROM User u WHERE u.userId = :userId AND u.secession = :secession")
    Optional<UserResponseDto> findUserByUserId(@Param("userId") String userId, @Param("secession") int secession);
}
