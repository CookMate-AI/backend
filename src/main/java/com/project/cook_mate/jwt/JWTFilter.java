package com.project.cook_mate.jwt;

import com.project.cook_mate.user.dto.CustomUserDetails;
import com.project.cook_mate.user.model.User;
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


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
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

//            filterChain.doFilter(request, response);

            //토큰이 없는 경우이기에 filterchain을 끊고 메서드 종료
            return;

        }
        System.out.println("authorization now");
        String token = authorization.split(" ")[1]; // 접두사 제거 - bearer 제거

        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid JWT Token\"}");
            return;
        }

        //토큰 유효시간 검증
        if(jwtUtil.isExpired(token)){
            System.out.println("token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token expired\"}");
//            filterChain.doFilter(request, response);
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
