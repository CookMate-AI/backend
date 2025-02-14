package com.project.cook_mate.user.service;

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
}
