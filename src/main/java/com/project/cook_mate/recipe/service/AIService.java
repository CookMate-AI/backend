package com.project.cook_mate.recipe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
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

    private final WebClient.Builder webClientBuilder;

    public Mono<List<String>> recommendMenu(String ingredients, int count) throws Exception{

        WebClient webClient = webClientBuilder.baseUrl(apiUrl).build();

        String prompt = String.format("%s로 만들 수 있는 요리명을 %d개 추천해줘. 다른 말 없이 요리명만 쉼표(,)로 구분해서 알려줘.", ingredients, count);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
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
                .map(response -> {
                    try {
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        if (candidates == null || candidates.isEmpty()) return List.of("추천 없음");

                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        if (content == null) return List.of("추천 없음");

                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts == null || parts.isEmpty()) return List.of("추천 없음");

                        String menuText = (String) parts.get(0).get("text");
                        System.out.println(menuText);

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

        String prompt = String.format("우선 %s를 카테고리에 맞게 구분해줘(한식:1, 양식:2, 중식:3, 일식:4, 그 외:5). " +
                "그리고 %s가 들어간 %s의 레시피를 한글로 알려줘. 레시피의 경우, 카테고리(숫자만)를 먼저 작성후 구분자(|)로 구분한 뒤에 출력해줘", food, ingredients, food);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
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
                .doOnError(error -> System.out.println("❌ 오류 발생: " + error.getMessage()))
                .map(response -> {
                    try {
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        if (candidates == null || candidates.isEmpty()) return new String[]{"추천없음"};

                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        if (content == null) return new String[]{"추천없음"};

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
}
