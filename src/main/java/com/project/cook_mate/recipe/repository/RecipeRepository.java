package com.project.cook_mate.recipe.repository;

import com.project.cook_mate.recipe.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
}
