package com.project.cook_mate.jwt.constant;

public final class SecurityConstants {
    // 생성자를 private으로 설정하여 인스턴스화 방지
    private SecurityConstants() {
        throw new AssertionError("Constants class should not be instantiated");
    }

    // JWT 관련 상수
    public static final class Token {
        public static final String ACCESS = "access";
        public static final String REFRESH = "refresh";
        public static final String AUTHORIZATION_HEADER = "Authorization";

        public static final long ACCESS_VALIDITY = 360000L;
        public static final long REFRESH_VALIDITY = 86400000L;
    }

    // 쿠키 관련 상수
    public static final class Cookie{
        public static final String REFRESH_NAME = "refresh";
        public static final int MAX_AGE = 24 * 60 * 60;
        public static final String SAME_SITE = "None";
    }
//    public static final String REFRESH_NAME = "refresh";
//    public static final int MAX_AGE = 24 * 60 * 60;

    // 공개 URL 패턴
    public static final class Endpoints {
        public static final String[] PUBLIC = {
                "/users/signup",
                "/users/check-id",
                "/users/check-Email/*",
                "/users/check-nickname",
                "/users/signin",
                "/users/find-id/*",
                "/users/find-pw",
                "/reissue"
        };

        public static final String[] SWAGGER = {
                "/swagger-ui/**",
                "/v3/api-docs/**"
        };
    }
}
