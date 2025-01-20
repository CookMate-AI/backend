package com.project.cook_mate.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedisScheduler {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 1시간마다 실행되는 작업
    @Scheduled(fixedRate = 1800000)  // 1시간마다 실행 (3600000ms) - 30분으로 설정해둠
    public void deleteExpiredAuthCodes() {
        System.out.println("만료된 인증번호 삭제 중");

        // 'auth:'로 시작하는 모든 키 확인
        redisTemplate.keys("auth:*").forEach(key -> {
            // 인증 코드가 null이면 만료된 것이므로 삭제
            String code = redisTemplate.opsForValue().get(key);
            if (code == null) {
                redisTemplate.delete(key);  // 만료된 키 삭제
                System.out.println("Deleted expired code: " + key);
            }
        });
    }
}