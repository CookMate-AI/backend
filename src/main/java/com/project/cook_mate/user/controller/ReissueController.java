package com.project.cook_mate.user.controller;


import com.project.cook_mate.jwt.JWTUtil;
import com.project.cook_mate.user.service.AuthService;
import io.jsonwebtoken.ExpiredJwtException;
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

        //get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refresh")) {

                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        // JWT 형식 검증
        int validationResult = jwtUtil.validateToken(refresh);
        if(validationResult == 1){
            return new ResponseEntity<>("expired token - try to login again", HttpStatus.NOT_ACCEPTABLE);
        }
        else if (validationResult != 0) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {
            //response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String userId = jwtUtil.getUserId(refresh);
        String role = jwtUtil.getRole(refresh);

        if(!authService.validateRefreshToken(userId, refresh)){
            return new ResponseEntity<>("refresh token not found or invalid", HttpStatus.UNAUTHORIZED);
        }

        //새 토큰 발급
        String newAccess = jwtUtil.createJwt("access", userId, role, 600000L); //600000 - 10분
        String newRefresh = jwtUtil.createJwt("refresh", userId, role, 86400000L);

        //레디스에 refresh 토큰 저장
        authService.saveRefreshToken(userId, newRefresh, 86400000L);

        //response
        response.setHeader("Authorization", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
