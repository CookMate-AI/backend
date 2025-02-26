package com.project.cook_mate.recipe.controller;

import com.project.cook_mate.recipe.dto.LoadRecipeResponseDto;
import com.project.cook_mate.recipe.dto.RecipeRequestDto;
import com.project.cook_mate.recipe.log.LogHelper2;
import com.project.cook_mate.recipe.service.RecipeService;
import com.project.cook_mate.user.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
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

    private final LogHelper2 logHelper2;

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
        String ingredients = (String) requestData.get("ingredients");
        String userId = customUserDetails.getUsername();

        return recipeService.getRecipe(ingredients, food, userId);
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

    @DeleteMapping("/my")
    public ResponseEntity<Map<String, String>> deleteRecipe(@RequestBody Map<String, Object> requestData,
                                                            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        int recipeId = (Integer)requestData.get("recipeId");
        String userId = customUserDetails.getUsername();

        try {
            return recipeService.deleteRecipe(recipeId, userId);
        }catch (Exception e){
            logHelper2.handleException(e);
            System.out.println(e);
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/my")
    public ResponseEntity<?> loadRecipe(@RequestParam(defaultValue = "0") int page,
                                                                  @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String userId = customUserDetails.getUsername();

        try {
            Page<LoadRecipeResponseDto> recipeList = recipeService.loadRecipe(page, userId);

            if(recipeList.getContent().isEmpty()){
                logHelper2.requestFail("레시피 불러오기 실패 - 레시피 없음", userId);
                return ResponseEntity.ok(Map.of("message", "불러올 레시피가 없습니다."));
            }
            logHelper2.requestSuccess("레시피 불러오기 성공", userId);
            return ResponseEntity.ok(recipeList.getContent());
        }catch (Exception e){
            logHelper2.handleException(e);
            System.out.println(e);
            return ResponseEntity.badRequest().build();
        }

    }
}
