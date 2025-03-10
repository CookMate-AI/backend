package com.project.cook_mate.user.service;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final StringRedisTemplate redisTemplate;

    //로그아웃 시 해당 토큰은 이용하면 문제가 있으므로 블랙리스트로 저장
    public void addToBlacklist(String token, long expiration) {
        redisTemplate.opsForValue().set(token, "BLACKLISTED", expiration, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }

    //로그인 성공시 refresh 토큰 저장
    public void saveRefreshToken(String userId, String refreshToken, long expiration){
        redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, expiration, TimeUnit.MILLISECONDS);
    }

    // 로그아웃 시 토큰 삭제
    public Cookie deleteRefreshToken(String userId) {
        redisTemplate.delete("refresh:" + userId);
        Cookie cookie = deleteCookie("refresh");
        return cookie;
    }

    public boolean validateRefreshToken(String userId, String refreshToken) {
        // Redis에서 저장된 토큰 조회
        String storedToken = redisTemplate.opsForValue().get("refresh:" + userId);

        // 저장된 토큰이 없거나 제출된 토큰과 일치하지 않으면 유효하지 않음
        return storedToken != null && storedToken.equals(refreshToken);
    }

    private Cookie deleteCookie(String key) {

        Cookie cookie = new Cookie(key, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        return cookie;
    }

}
