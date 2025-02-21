package com.project.cook_mate.recipe.controller;

import com.project.cook_mate.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public Mono<ResponseEntity<List<String>>> openRecipe(@RequestBody Map<String, Object> requestData){
        String food = (String) requestData.get("food");

        try {
            return recipeService.getRecipe(food, 1)
                    .map(menuList -> ResponseEntity.ok(menuList));
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }
}
