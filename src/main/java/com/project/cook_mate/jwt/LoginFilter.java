package com.project.cook_mate.jwt;

import com.project.cook_mate.user.dto.CustomUserDetails;
import com.project.cook_mate.user.log.LogHelper;
import com.project.cook_mate.user.model.User;
import com.project.cook_mate.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
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
            logHelper.requestFail("로그인 실패 - 탈퇴하였거나 없는 회원", username);
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
        System.out.println(role);

        String token = jwtUtil.createJwt(userId, role, 60*60*1000L);

        logHelper.requestSuccess("로그인 성공", userId);

        response.addHeader("Authorization", "Bearer " + token);
        response.addHeader("User-Nickname", encodedNickname);

    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        logHelper.requestFail("로그인 실패 - id나 pw 틀림", obtainUsername(request));
        response.setStatus(401);

    }
}
