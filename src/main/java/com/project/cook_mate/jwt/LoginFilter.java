package com.project.cook_mate.jwt;

import com.project.cook_mate.jwt.constant.SecurityConstants;
import com.project.cook_mate.user.dto.CustomUserDetails;
import com.project.cook_mate.user.log.LogHelper;
import com.project.cook_mate.user.model.User;
import com.project.cook_mate.user.repository.UserRepository;
import com.project.cook_mate.user.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

//로그인 확인 담당
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager; //검증 담당
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuthService authService;

    private final LogHelper logHelper;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        logHelper.processUserRequest("로그인", username);

        Optional<User> user = userRepository.findByUserIdAndSecession(username, 0);

        if (user.isEmpty()) {
//            log.warn("Login attempt failed: User not found or withdrawn - {}", username);
            throw new UsernameNotFoundException("User not found");
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password, null);
        return authenticationManager.authenticate(authToken);


    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = customUserDetails.getUsername();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // 닉네임 인코딩
        String encodedNickname = Base64.getEncoder().encodeToString(
                customUserDetails.getNickName().getBytes(StandardCharsets.UTF_8)
        );


        clearExistingRefreshToken(request, response);


        //토큰 생성
        JWTUtil.TokenPayload accessPayload = new JWTUtil.TokenPayload(
                userId, role, SecurityConstants.Token.ACCESS
        );
        JWTUtil.TokenPayload refreshPayload = new JWTUtil.TokenPayload(
                userId, role, SecurityConstants.Token.REFRESH
        );

        String accessToken = jwtUtil.createToken(accessPayload, SecurityConstants.Token.ACCESS_VALIDITY);
        String refreshToken = jwtUtil.createToken(refreshPayload, SecurityConstants.Token.REFRESH_VALIDITY);


        authService.saveRefreshToken(userId, refreshToken, SecurityConstants.Token.REFRESH_VALIDITY);

        // 응답 설정
        response.setHeader(SecurityConstants.Token.AUTHORIZATION_HEADER, accessToken);
        response.setHeader("User-Nickname", encodedNickname);
        response.addCookie(createCookie(SecurityConstants.Cookie.REFRESH_NAME, refreshToken));
        response.setStatus(HttpStatus.OK.value());

    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        if (failed instanceof UsernameNotFoundException) {
            // 탈퇴하였거나 없는 회원인 경우
            logHelper.requestFail("로그인 실패 - 탈퇴하였거나 없는 회원", obtainUsername(request));
            response.setStatus(404);
        } else if (failed instanceof BadCredentialsException) {
            // ID/PW 불일치인 경우
            logHelper.requestFail("로그인 실패 - id나 pw 틀림", obtainUsername(request));
            response.setStatus(401); // Unauthorized
        } else {
            // 기타 인증 실패
            logHelper.requestFail("로그인 실패 - 기타 오류", obtainUsername(request));
            response.setStatus(500);
        }

    }

    private void clearExistingRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SecurityConstants.Cookie.REFRESH_NAME.equals(cookie.getName())) {
                    Cookie expiredCookie = new Cookie(cookie.getName(), null);
                    expiredCookie.setMaxAge(0);
                    expiredCookie.setPath(cookie.getPath());
                    response.addCookie(expiredCookie);
                }
            }
        }
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
