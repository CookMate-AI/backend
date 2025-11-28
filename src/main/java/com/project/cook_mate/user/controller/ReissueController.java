package com.project.cook_mate.user.controller;


import com.project.cook_mate.jwt.JWTUtil;
import com.project.cook_mate.jwt.TokenValidationResult;
import com.project.cook_mate.jwt.constant.SecurityConstants;
import com.project.cook_mate.user.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReissueController {
    private final JWTUtil jwtUtil;
    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("REISSUE 들어옴");

        //get refresh token
        String refresh = extractRefreshToken(request);
        if (refresh == null) {
            return new ResponseEntity<>("refresh token이 없습니다", HttpStatus.BAD_REQUEST);
        }

        // 토큰 검증
        TokenValidationResult validationResult = jwtUtil.validateToken(refresh);
        if (!validationResult.isValid()) {
            if (validationResult.isExpired()) {
                return new ResponseEntity<>("만료된 토큰입니다 - 다시 로그인해주세요", HttpStatus.NOT_ACCEPTABLE);
            }
            return new ResponseEntity<>("유효하지 않은 refresh token입니다", HttpStatus.BAD_REQUEST);
        }

        // 토큰 페이로드 추출
        JWTUtil.TokenPayload payload = jwtUtil.extractPayload(refresh);

        // Refresh 토큰 타입 확인
        if (!SecurityConstants.Token.REFRESH.equals(payload.getCategory())) {
            return new ResponseEntity<>("유효하지 않은 refresh token입니다", HttpStatus.BAD_REQUEST);
        }

        // Redis에서 Refresh 토큰 검증
        if (!authService.validateRefreshToken(payload.getUserId(), refresh)) {
            return new ResponseEntity<>("저장된 refresh token을 찾을 수 없거나 유효하지 않습니다", HttpStatus.UNAUTHORIZED);
        }


        // 새 토큰 발급
        String newAccess = jwtUtil.createToken(
                new JWTUtil.TokenPayload(payload.getUserId(), payload.getRole(), SecurityConstants.Token.ACCESS),
                SecurityConstants.Token.ACCESS_VALIDITY
        );

        String newRefresh = jwtUtil.createToken(
                new JWTUtil.TokenPayload(payload.getUserId(), payload.getRole(), SecurityConstants.Token.REFRESH),
                SecurityConstants.Token.REFRESH_VALIDITY
        );


        //레디스에 refresh 토큰 저장
        authService.saveRefreshToken(payload.getUserId(), newRefresh, SecurityConstants.Token.REFRESH_VALIDITY);


        //response
        response.setHeader(SecurityConstants.Token.AUTHORIZATION_HEADER, newAccess);
        response.addCookie(createCookie(SecurityConstants.Cookie.REFRESH_NAME, newRefresh));


        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SecurityConstants.Cookie.REFRESH_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }


    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(SecurityConstants.Cookie.MAX_AGE);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "None");
        cookie.setSecure(true);
        return cookie;

    }
}
