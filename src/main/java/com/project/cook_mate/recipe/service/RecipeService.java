package com.project.cook_mate.recipe.service;

import com.project.cook_mate.category.model.Category;
import com.project.cook_mate.category.repository.CategoryRepository;
import com.project.cook_mate.recipe.dto.RecipeRequestDto;
import com.project.cook_mate.recipe.dto.RecipeResponseDto;
import com.project.cook_mate.recipe.model.Recipe;
import com.project.cook_mate.recipe.repository.RecipeRepository;
import com.project.cook_mate.user.model.User;
import com.project.cook_mate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient.Builder webClientBuilder;

    public Mono<List<String>> recommendMenu(String ingredients, int count) throws Exception{
        return aiService.recommendMenu(ingredients,1);

    }

    @Transactional(readOnly = true)
    public Mono<ResponseEntity<Map<String, Object>>> getRecipe(String food, String userId){

        Optional<RecipeResponseDto> recipe = recipeRepository.findByFoodNameAndUserId(food, userId);

        if(recipe.isPresent()){
            RecipeResponseDto recipeResponseDto = recipe.get();
            int category = recipeResponseDto.getCategory();
            String content = recipeResponseDto.getContent();
            return Mono.just(ResponseEntity.ok(Map.of("category", category, "recipe", content, "isSaved", "O")));
        }
        else {
            try {
                return aiService.getRecipe(food)
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
                            return ResponseEntity.ok(Map.of("category", category, "recipe", result[1], "isSaved", "X"));
                        });
            } catch (Exception e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }
        }

    }

    public ResponseEntity<Map<String, String>> saveRecipe(RecipeRequestDto recipeRequestDto, String userId){
        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<Category> optionalCategory = categoryRepository.findById(recipeRequestDto.getCategory());

        if(optionalUser.isEmpty() || optionalCategory.isEmpty()){
            System.out.println("해당 유저 혹은 해당 카테고리 없음");
            return ResponseEntity.badRequest().body(Map.of("message", "해당 유저 혹은 해당 카테고리 없음"));
        }else{
            Recipe recipe = recipeRequestDto.toEntity(optionalUser.get(), optionalCategory.get());
            recipeRepository.save(recipe);
            return ResponseEntity.ok().body(Map.of("message", "레시피 저장 완료"));
        }

    }

}
