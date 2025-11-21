package com.project.cook_mate.recipe.service;

import com.project.cook_mate.recipe.log.LogHelper2;
import com.project.cook_mate.user.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private static final String MODEL_NAME = "gemini-2.5-flash";

    private final WebClient.Builder webClientBuilder;

    private final LogHelper2 logHelper;

    public Mono<List<String>> recommendMenu(String ingredients, int count) throws Exception{

        String id = getId();
        logHelper.processRecipeRequest("메뉴 추천 기능", id);

        WebClient webClient = webClientBuilder.baseUrl(apiUrl).build();

        String prompt = String.format("다음은 사용자가 제공한 재료 목록입니다: %s. "
                + "이 재료들이 실제 음식 재료인지 검토한 후, 전부 음식재료라면 해당 재료들이 다 들어간 "
                + "적절한 요리명을 %d개 추천해줘. 추천 해줄때는 너무 큰 범주의 요리명 말고 자세한 요리명으로 부탁해." +
                "예를 들어 카레면 카레로 알려주는게 아닌 일본식 카레와 같이 정확해야 해." +
                "왜냐하면 해당 요리 명을 고르면 한식, 일식, 중식 등으로 지정할 거라 명확해야 해." +
                "그리고 다른 말 없이 요리명만 쉼표(,)로 구분해서 알려줘."
                + "만약 재료가 의미 없는 단어이거나 이상하다면 '잘못된 입력'이라고 답해줘. ", ingredients, count);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/models/" + MODEL_NAME + ":generateContent")
                        .queryParam("key", apiKey).build())
                .header("Content-Type", "application/json") // ✅ 헤더 추가
                .bodyValue(Map.of(
                        "contents", List.of(
                                Map.of(
                                        "parts", List.of(
                                                Map.of("text", prompt)
                                        )
                                )
                        )
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(error -> {
                            logHelper.handleException((Exception) error);
                            System.out.println("❌ 오류 발생: " + error.getMessage());
                        }
                )
                .map(response -> {
                    try {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        if (candidates == null || candidates.isEmpty()) return List.of("추천 없음");

                        @SuppressWarnings("unchecked")
                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        if (content == null) return List.of("추천 없음");

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts == null || parts.isEmpty()) return List.of("추천 없음");

                        String menuText = (String) parts.get(0).get("text");
                        System.out.println(menuText);
                        if ("잘못된 입력".equals(menuText.trim())) {
                            logHelper.requestFail("메뉴 추천 기능 실패 - 정상적인 입력X", id);
                            throw new ClassCastException("잘못된 재료 입력값");
                        }

                        logHelper.requestSuccess("메뉴추천 기능 성공", id);

                        return Arrays.stream(menuText.split(","))
                                .map(String::trim) // 공백 제거
                                .filter(s -> !s.isEmpty()) // 빈 문자열 제거
                                .collect(Collectors.toList());

                    } catch (ClassCastException e) {
                        return List.of("응답 형식 오류");
                    }
                });
    }

    public Mono<String[]> getRecipe(String ingredients, String food) throws Exception{

        WebClient webClient = webClientBuilder.baseUrl(apiUrl).build();

        String prompt = String.format("우선 %s를 카테고리에 맞게 구분해줘(한식:1, 양식:2, 중식:3, 일식:4, 기타 혹은 구분 어려운 경우:5). " +
                "그리고 %s가 들어간 %s의 레시피를 1개만 한글로 알려주는데 재료의 종류나 양 그리고 각 과정에서 소요되는 시간도 제대로 명시를 해줘." +
                "만약 재료가 개수가 지정이 안된경우엔 2인분 기준의 레시피로 작성해줘" +
                "레시피 설명에 대한 목차 순서는 재료, 레시피, 팁 순으로 작성해주고 레시피 부분의 경우 서론을 붙이지 말고 레시피만 적어줘" +
                "그리고 레시피의 경우, 작성할 때 앞에 아무것도 쓰지말고 카테고리에 해당하는 숫자를 먼저 작성후 구분자(|)로 구분한 뒤에 출력해줘" +
                "예시) 2 | 레시피부분", food, ingredients, food);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/models/" + MODEL_NAME + ":generateContent")
                        .queryParam("key", apiKey).build())
                .header("Content-Type", "application/json") // ✅ 헤더 추가
                .bodyValue(Map.of(
                        "contents", List.of(
                                Map.of(
                                        "parts", List.of(
                                                Map.of("text", prompt)
                                        )
                                )
                        )
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(response -> System.out.println("🔹 API 응답: " + response))
                .doOnError(error -> {
                            System.out.println("❌ 오류 발생: " + error.getMessage());
                        }
                )
                .map(response -> {
                    try {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        if (candidates == null || candidates.isEmpty()) return new String[]{"추천없음"};

                        @SuppressWarnings("unchecked")
                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        if (content == null) return new String[]{"추천없음"};

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts == null || parts.isEmpty()) return new String[]{"추천없음"};

                        String result = (String) parts.get(0).get("text");
                        String[] arr = result.split("\\|");

                        return arr;


                    } catch (ClassCastException e) {
                        return new String[]{"추천없음"};
                    }
                });
    }

    private String getId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
