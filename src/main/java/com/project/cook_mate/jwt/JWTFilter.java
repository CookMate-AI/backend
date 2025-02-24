package com.project.cook_mate.jwt;

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

//jwt 검증
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final AuthService authService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        System.out.println(requestURI);
        if (requestURI.equals("/users/signup") || requestURI.equals("/users/check-id") ||
                        requestURI.equals("/users/check-Email/send-Email") || requestURI.equals("/users/check-Email/certification") ||
                        requestURI.equals("/users/check-Nname") || requestURI.equals("/users/signin") ||
                        requestURI.equals("/users/find-id/send-Email") || requestURI.equals("/users/find-id/certification") ||
                        requestURI.equals("/users/find-pw")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        //request에서 헤더를 가져오고
        String authorization = request.getHeader("Authorization");

        //authorization 헤더 검증
        if(authorization == null || !authorization.startsWith("Bearer ")){
            System.out.println("token null");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"no Token\"}");

            return;

        }
        System.out.println("authorization now");

        String token = jwtUtil.extractToken(request);

        // 블랙리스트 확인 (로그아웃한 토큰인지)
        if (token != null && authService.isBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"로그아웃된 토큰입니다.\"}");
            return;
        }

        int check = jwtUtil.validateToken(token);

        if (check == 2) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"토큰 문제\"}");
            return;
        }

        //토큰 유효시간 검증
        else if(check == 1){
            System.out.println("token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"토큰이 만료되었습니다. 다시 로그인 부탁드립니다.\"}");
            return;
        }
        System.out.println("토큰 이상X");

        String userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token); //회원만 있기에 user 로 가져옴

        User user = new User();
        user.setUserId(userId);
        user.setUserPw("temppassword"); //매번 db로 확인하면 효율적으로 문제가 생겨 임의로 지정
        user.setRole(role);

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        //세션에 사용자 등록 - user세션 생성
        SecurityContextHolder.getContext().setAuthentication(authToken);
        System.out.println("마무리 작업");

        filterChain.doFilter(request,response);

    }
}
