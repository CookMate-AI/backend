package com.project.cook_mate.recipe.service;

import com.project.cook_mate.category.model.Category;
import com.project.cook_mate.category.repository.CategoryRepository;
import com.project.cook_mate.recipe.dto.LoadRecipeResponseDto;
import com.project.cook_mate.recipe.dto.RecipeRequestDto;
import com.project.cook_mate.recipe.dto.RecipeResponseDto;
import com.project.cook_mate.recipe.log.LogHelper2;
import com.project.cook_mate.recipe.model.Recipe;
import com.project.cook_mate.recipe.repository.RecipeRepository;
import com.project.cook_mate.user.dto.CustomUserDetails;
import com.project.cook_mate.user.model.User;
import com.project.cook_mate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RecipeService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RecipeRepository recipeRepository;

    private final AIService aiService;

    private final LogHelper2 logHelper;

    public Mono<List<String>> recommendMenu(String ingredients, int count) throws Exception{
        return aiService.recommendMenu(ingredients,count);

    }

    @Transactional(readOnly = true)
    public Mono<ResponseEntity<Map<String, Object>>> getRecipe(String ingredients, String food, String userId){
        logHelper.processRecipeRequest("레시피 받기", userId);

        Optional<RecipeResponseDto> recipe = recipeRepository.findByFoodNameAndUserId(food, userId);

        if(recipe.isPresent()){
            RecipeResponseDto recipeResponseDto = recipe.get();
            int category = recipeResponseDto.getCategory();
            String content = recipeResponseDto.getContent();
            logHelper.requestSuccess("레시피 받기 성공 - DB에 존재", userId);
            return Mono.just(ResponseEntity.ok(Map.of("category", category, "recipe", content, "isSaved", "O")));
        }
        else {
            try {
                return aiService.getRecipe(ingredients, food)
                        .map(result -> {
                            if (result.length < 2) {
                                logHelper.requestFail("레시피 받기 실패 - AI 결과없음", userId);
                                return ResponseEntity.badRequest().body(Map.of("error", "잘못된 응답 형식"));
                            }
                            int category;
                            try {
                                category = Integer.parseInt(result[0].trim());
                            } catch (NumberFormatException e) {
                                logHelper.handleException(e);
                                return ResponseEntity.badRequest().body(Map.of("error", "카테고리 변환 실패"));
                            }
                            logHelper.requestSuccess("레시피 받기 성공 - AI 정상 응답", userId);
                            return ResponseEntity.ok(Map.of("category", category, "recipe", result[1], "isSaved", "X"));
                        });
            } catch (Exception e) {
                logHelper.handleException(e);
                throw new RuntimeException(e);
            }
        }

    }

    public ResponseEntity<Map<String, String>> saveRecipe(RecipeRequestDto recipeRequestDto, String userId){
        String foodName = recipeRequestDto.getFood();
        String requestMessage = String.format("레시피 저장 (음식 명: %s)", foodName);
        logHelper.processRecipeRequest(requestMessage, userId);

        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<Category> optionalCategory = categoryRepository.findById(recipeRequestDto.getCategory());

        if(optionalUser.isEmpty() || optionalCategory.isEmpty()){
            logHelper.requestFail("레시피 저장 실패 - 유저 혹은 카테고리 없음", userId);
            System.out.println("해당 유저 혹은 해당 카테고리 없음");
            return ResponseEntity.badRequest().body(Map.of("message", "해당 유저 혹은 해당 카테고리 없음"));
        }else{
            Recipe recipe = recipeRequestDto.toEntity(optionalUser.get(), optionalCategory.get());
            recipeRepository.save(recipe);
            logHelper.requestSuccess("레시피 저장 성공", userId);
            return ResponseEntity.ok().body(Map.of("message", "레시피 저장 완료"));
        }

    }

    public ResponseEntity<Map<String, String>> deleteRecipe(int recipeId, String userId){
        String requestMessage = String.format("레시피 삭제 (레시피 ID: %s)", recipeId);
        logHelper.processRecipeRequest(requestMessage, userId);
        Optional<Recipe> optionalRecipe = recipeRepository.findByRecipeIdAndUser_UserId(recipeId, userId);

        if(optionalRecipe.isEmpty()){
            logHelper.requestFail("레시피 삭제 불가 - 레시피 id 없음", userId);
            System.out.println("해당 레시피 id없음");
            return ResponseEntity.badRequest().body(Map.of("message", "해당 레시피 id없음"));
        }else{
            logHelper.requestSuccess("레시피 삭제 성공", userId);
            Recipe recipe = optionalRecipe.get();
            recipeRepository.delete(recipe);
            return ResponseEntity.ok().body(Map.of("message", "레시피 삭제 완료"));
        }

    }

    public Page<LoadRecipeResponseDto> loadRecipe(int page, String userId){
        logHelper.processRecipeRequest("저장한 레시피 불러오기", userId);

        int size = 12;

        Pageable pageable = PageRequest.of(page, size);

        return recipeRepository.findRecipesByUserId(userId, pageable);

    }

}
