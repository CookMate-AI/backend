package com.project.cook_mate.jwt.constant;

public final class SecurityConstants {
    // 생성자를 private으로 설정하여 인스턴스화 방지
    private SecurityConstants() {
        throw new AssertionError("Constants class should not be instantiated");
    }

    // JWT 관련 상수
    public static final String ACCESS_TOKEN = "access";
    public static final String REFRESH_TOKEN = "refresh";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // 토큰 유효 시간 (ms)
    public static final long ACCESS_TOKEN_VALIDITY = 360000L; // 600000L = 10분
    public static final long REFRESH_TOKEN_VALIDITY = 86400000L;

    // 쿠키 관련 상수
    public static final String REFRESH_COOKIE_NAME = "refresh";
    public static final int COOKIE_MAX_AGE = 24 * 60 * 60;

    // 공개 URL 패턴
    public static final String[] PUBLIC_URLS = {
            "/users/signup",
            "/users/check-id",
            "/users/check-Email/send-Email",
            "/users/check-Email/certification",
            "/users/check-nickname",
            "/users/signin",
            "/users/find-id/send-Email",
            "/users/find-id/certification",
            "/users/find-pw",
            "/reissue"
    };
}
