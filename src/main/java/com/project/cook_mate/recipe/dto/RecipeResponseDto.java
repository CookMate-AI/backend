package com.project.cook_mate.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponseDto {
    private Integer recipeId;
    private String foodName;
    private String content;
    private String userId;
    private Integer category;
}
