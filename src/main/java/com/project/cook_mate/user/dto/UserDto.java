package com.project.cook_mate.user.dto;

import com.project.cook_mate.user.model.User;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String userId;
    private String userPw;
//    private String nickName;
    private String email;
//    private int secession;
//    private LocalDateTime joinDate;
//    private LocalDateTime updateDate;

    public User toEntity(String pw, String nickName){
        return User.builder()
                .userId(this.userId)
                .userPw(pw)
                .nickName(nickName)
                .email(this.email)
                .build();

    }
}
