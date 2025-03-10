package com.project.cook_mate.jwt;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
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

        if(user.isPresent()){
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        return authenticationManager.authenticate(authToken);
        }else{
            System.out.println("탈퇴하거나 없는 회원");
            throw new UsernameNotFoundException("User not found");
        }

    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = customUserDetails.getUsername();
        String nickname = customUserDetails.getNickName();
        String encodedNickname = Base64.getEncoder().encodeToString(nickname.getBytes(StandardCharsets.UTF_8));

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("쿠키 발견: " + cookie.getName() + ", 경로: " + cookie.getPath());
                if ("refresh".equals(cookie.getName())) {
                    Cookie expiredCookie = new Cookie(cookie.getName(), null);
                    expiredCookie.setMaxAge(0);
                    expiredCookie.setPath(cookie.getPath());
                    response.addCookie(expiredCookie);
                }
            }
        }

        //토큰 생성
        String access = jwtUtil.createJwt("access", userId, role, 600000L); // 앞에 3 빼줘야 함 지금은 테스트 용
        String refresh = jwtUtil.createJwt("refresh", userId, role, 86400000L);

        authService.saveRefreshToken(userId, refresh, 86400000L);

        response.setHeader("Authorization", access);
        response.setHeader("User-Nickname", encodedNickname);
        response.addCookie(createCookie("refresh",refresh));
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

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true); //Https 적용시
        cookie.setPath("/"); //쿠키가 적용될 범위 설정 시
        cookie.setHttpOnly(true); //js로 해당 쿠키 접근 못하게 설정

        return cookie;
    }
}
