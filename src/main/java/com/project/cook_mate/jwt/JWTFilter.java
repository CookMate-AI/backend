package com.project.cook_mate.jwt;

import com.project.cook_mate.jwt.constant.SecurityConstants;
import com.project.cook_mate.user.dto.CustomUserDetails;
import com.project.cook_mate.user.model.User;
import com.project.cook_mate.user.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Optional;

//jwt 검증
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final AuthService authService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isPublicUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }


        try {
            processTokenAuthentication(request, response, filterChain);
        } catch (Exception e) {
//            log.error("Authentication error: ", e);
            handleAuthenticationError(response, e.getMessage());
        }

    }

    //URL 검증
    private boolean isPublicUrl(HttpServletRequest request) {
        return Arrays.asList(SecurityConstants.PUBLIC_URLS)
                .contains(request.getRequestURI());
    }

    //토큰 인증 과정
    private void processTokenAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        Optional<String> tokenOptional = jwtUtil.extractTokenFromRequest(request);

        if (tokenOptional.isEmpty()) {
            handleMissingToken(response);
            return;
        }

        String token = tokenOptional.get();

        // 블랙리스트 확인
        if (authService.isBlacklisted(token)) {
            handleBlacklistedToken(response);
            return;
        }

        TokenValidationResult validationResult = jwtUtil.validateToken(token);

        if (!validationResult.isValid()) {
            if (validationResult.isExpired()) {
                handleExpiredToken(response);
            } else {
                handleInvalidToken(response, validationResult.getErrorMessage());
            }
            return;
        }

        JWTUtil.TokenPayload payload = jwtUtil.extractPayload(token);

        if (!SecurityConstants.ACCESS_TOKEN.equals(payload.getCategory())) {
            handleInvalidTokenType(response);
            return;
        }

        authenticateUser(payload);
        filterChain.doFilter(request, response);
    }

    private void authenticateUser(JWTUtil.TokenPayload payload) {
        User user = new User();
        user.setUserId(payload.getUserId());
        user.setRole(payload.getRole());

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                customUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }


    //Token error
    private void handleMissingToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"토큰이 없습니다\"}");
    }

    private void handleBlacklistedToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("{\"error\": \"로그아웃된 토큰입니다\"}");
    }

    private void handleExpiredToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("{\"error\": \"토큰이 만료되었습니다. refresh 요청이 필요합니다\"}");
    }

    private void handleInvalidToken(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(String.format("{\"error\": \"잘못된 토큰입니다: %s\"}", message));
    }

    private void handleInvalidTokenType(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("{\"error\": \"유효하지 않은 토큰 타입입니다\"}");
    }

    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(String.format("{\"error\": \"인증 처리 중 오류가 발생했습니다: %s\"}", message));
    }


}
