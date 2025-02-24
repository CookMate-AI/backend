package com.project.cook_mate.recipe.repository;

import com.project.cook_mate.recipe.dto.LoadRecipeResponseDto;
import com.project.cook_mate.recipe.dto.RecipeResponseDto;
import com.project.cook_mate.recipe.model.Recipe;
import com.project.cook_mate.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    @Query("SELECT new com.project.cook_mate.recipe.dto.RecipeResponseDto(r.recipeId, r.foodName, r.content, u.userId, c.categoryId) " +
            "FROM Recipe r " +
            "JOIN r.user u " +
            "JOIN r.category c " +
            "WHERE r.foodName = :foodName AND u.userId = :userId")
    Optional<RecipeResponseDto> findByFoodNameAndUserId(@Param("foodName") String foodName, @Param("userId") String userId);

    Optional<Recipe> findByRecipeIdAndUser_UserId(int recipeId, String userId);

    @Query("SELECT new com.project.cook_mate.recipe.dto.LoadRecipeResponseDto(r.recipeId, r.foodName, r.content, c.categoryId) " +
            "FROM Recipe r " +
            "JOIN r.category c " +
            "WHERE r.user.userId = :userId " +
            "ORDER BY r.recipeId ASC")
    Page<LoadRecipeResponseDto> findRecipesByUserId(@Param("userId") String userId, Pageable pageable);

}
