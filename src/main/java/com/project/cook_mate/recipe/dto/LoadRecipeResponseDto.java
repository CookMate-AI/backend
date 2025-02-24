package com.project.cook_mate.recipe.dto;

import com.project.cook_mate.recipe.model.Recipe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoadRecipeResponseDto {
    private Integer recipeId;
    private String foodName;
    private String content;
    private Integer category;

    public LoadRecipeResponseDto(Recipe recipe) {
        recipeId = recipe.getRecipeId();
        foodName = recipe.getFoodName();
        content = recipe.getContent();
        category = recipe.getCategory().getCategoryId();
    }
}
