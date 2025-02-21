package com.project.cook_mate.recipe.controller;

import com.project.cook_mate.recipe.dto.RecipeRequestDto;
import com.project.cook_mate.recipe.service.RecipeService;
import com.project.cook_mate.user.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recipe")
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping("/menu")
    public Mono<ResponseEntity<List<String>>> recommendMenu(@RequestBody Map<String, Object> requestData){
        String ingredients = (String) requestData.get("ingredients");

        try {
            return recipeService.recommendMenu(ingredients, 2)
                    .map(menuList -> ResponseEntity.ok(menuList));
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/recommend")
    public Mono<ResponseEntity<Map<String, Object>>> openRecipe(@RequestBody Map<String, Object> requestData){
        String food = (String) requestData.get("food");

        try {
            return recipeService.getRecipe(food)
                    .map(result -> {
                        if (result.length < 2) {
                            return ResponseEntity.badRequest().body(Map.of("error", "잘못된 응답 형식"));
                        }
                        int category;
                        try {
                            category = Integer.parseInt(result[0].trim());
                        } catch (NumberFormatException e) {
                            return ResponseEntity.badRequest().body(Map.of("error", "카테고리 변환 실패"));
                        }
                        return ResponseEntity.ok(Map.of("category", category, "recipe", result[1]));
                    });
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveRecipe(@RequestBody RecipeRequestDto requestData,
                                                          @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String userId = customUserDetails.getUsername();

        try {
            return recipeService.saveRecipe(requestData, userId);
        }catch (Exception e){
            System.out.println(e);
            return ResponseEntity.badRequest().build();
        }


    }
}
