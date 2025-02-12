package com.project.cook_mate.user.dto;

import com.project.cook_mate.recipe.model.Recipe;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private String userId;

    private String nickName;

    private String email;

    private LocalDateTime joinDate;

    private LocalDateTime updateDate;

    private String role;

}
