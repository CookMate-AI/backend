package com.project.cook_mate.recipe.dto;

import com.project.cook_mate.category.model.Category;
import com.project.cook_mate.recipe.model.Recipe;
import com.project.cook_mate.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRequestDto {

    private String food;

    private String content;

    private int category;

    public Recipe toEntity(User user, Category category){
        return Recipe.builder()
                .foodName(food)
                .content(content)
                .user(user)
                .category(category)
                .build();

    }

}
