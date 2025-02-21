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
    public Mono<ResponseEntity<Map<String, Object>>> openRecipe(@RequestBody Map<String, Object> requestData,
                                                                @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String food = (String) requestData.get("food");
        String userId = customUserDetails.getUsername();

        return recipeService.getRecipe(food, userId);
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
