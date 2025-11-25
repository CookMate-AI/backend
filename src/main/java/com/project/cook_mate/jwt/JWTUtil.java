package com.project.cook_mate.jwt;


import com.project.cook_mate.jwt.constant.SecurityConstants;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
public class JWTUtil {
    private final SecretKey secretKey;
    private final JwtParser jwtParser;


    public JWTUtil(@Value("${spring.jwt.secret}") String secret){

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());

        this.jwtParser = Jwts.parser().verifyWith(secretKey).build();

    }

    // TokenPayload DTO 추가
    @lombok.Value
    public static class TokenPayload {
        String userId;
        String role;
        String category;
    }

    public TokenPayload extractPayload(String token) {
        Claims claims = getClaims(token);
        return new TokenPayload(
                claims.get("userId", String.class),
                claims.get("role", String.class),
                claims.get("category", String.class)
        );
    }

    private Claims getClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

//
//    public String getUserId(String token) {
//
//        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId", String.class);
//    }
//
//    public String getRole(String token) {
//
//        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
//    }
//
//    public String getCategory(String token) {
//
//        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
//    }

    public boolean isExpired(String token) {

        try {
            return getClaims(token)
                    .getExpiration()
                    .before(new Date());
        } catch (JwtException e) {
//            log.error("토큰 만료 확인 중 에러 발생", e);
            return true;
        }

    }

//    //토큰 생성
//    public String createJwt(String category, String userId, String role, Long expiredMs) {
//
//        return Jwts.builder()
//                .claim("category", category)
//                .claim("userId", userId)
//                .claim("role", role)
//                .issuedAt(new Date(System.currentTimeMillis()))
//                .expiration(new Date(System.currentTimeMillis() + expiredMs))
//                .signWith(secretKey)
//                .compact();
//    }

    public String createToken(TokenPayload payload, long expirationTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .claim("category", payload.getCategory())
                .claim("userId", payload.getUserId())
                .claim("role", payload.getRole())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Optional<String> extractTokenFromRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER));
    }

    public TokenValidationResult validateToken(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return TokenValidationResult.valid();
        } catch (ExpiredJwtException e) {
//            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            return TokenValidationResult.expired();
        } catch (JwtException e) {
//            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            return TokenValidationResult.invalid(e.getMessage());
        }
    }



//    //토큰 검증
//    public int validateToken(String token) {
//        try {
//            Jwts.parser()
//                    .verifyWith(secretKey)
//                    .build()
//                    .parseSignedClaims(token);
//            return 0;
//        } catch (SecurityException | MalformedJwtException e) {
//            System.out.println("잘못된 JWT 서명입니다.");
//        } catch (ExpiredJwtException e) {
//            System.out.println("만료된 JWT 토큰입니다.");
//            return 1;
//        } catch (UnsupportedJwtException e) {
//            System.out.println("지원되지 않는 JWT 토큰입니다.");
//        }catch (io.jsonwebtoken.security.SignatureException e) {
//            System.out.println("잘못된 토큰입니다.");
//        } catch (IllegalArgumentException e) {
//            System.out.println("JWT 토큰이 잘못되었습니다.");
//        }
//        return 2;
//    }

    //토큰 만료시간 가져오기 (토큰이 끝나는 시간 - 현재 시간 = 남은 토큰 유지 시간)
    public long getExpirationTime(String token) {
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

//    // 토큰에서 추출하는 메서드
//    public String extractToken(HttpServletRequest request) {
//        String header = request.getHeader("Authorization");
//        if (header != null) {
//            return header;
//        }
//        return null;
//    }


}
