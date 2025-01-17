package com.project.cook_mate.user.model;

import com.project.cook_mate.recipe.model.Recipe;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
@Builder
public class User {
    @Id
    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "user_pw", nullable = false)
    private String userPw;

    @Column(name = "nickname", nullable = false)
    private String nickName;

    @Column(name = "email", nullable = false)
    private String email;

    @Builder.Default
    @Column
    private int secession = 0; //0은 탈퇴X, 1은 탈퇴상태

    @Column(name = "joindate", nullable = false)
    private LocalDateTime joinDate;

    @Column(name = "updatedate")
    private LocalDateTime updateDate;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Recipe> recipes;
}
