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
import java.io.PrintWriter;

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
                        requestURI.equals("/users/check-nickname") || requestURI.equals("/users/signin") ||
                        requestURI.equals("/users/find-id/send-Email") || requestURI.equals("/users/find-id/certification") ||
                        requestURI.equals("/users/find-pw") || requestURI.equals("/reissue")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        //request에서 헤더를 가져오고
        String authorization = request.getHeader("Authorization");

        //authorization 헤더 검증
        if(authorization == null){
            System.out.println("token null");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"no Token\"}");

            return;

        }
        System.out.println("authorization now");

//        String token = jwtUtil.extractToken(request);

        // 블랙리스트 확인 (로그아웃한 토큰인지)
        if (authorization != null && authService.isBlacklisted(authorization)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"로그아웃된 토큰입니다.\"}");
            return;
        }

        int check = jwtUtil.validateToken(authorization);

        if (check == 2) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"정상적이지 않은 토큰 문제\"}");
            return;
        }

        //토큰 유효시간 검증
        else if(check == 1){
            System.out.println("token expired");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE); //refresh 토큰을 통한 재발급을 위한 상태코드 (406)
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"토큰이 만료되었습니다. refresh 요청 필요\"}");
            return;
        }

        String category = jwtUtil.getCategory(authorization);

        if (!category.equals("access")) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        String userId = jwtUtil.getUserId(authorization);
        String role = jwtUtil.getRole(authorization); //회원만 있기에 user 로 가져옴

        User user = new User();
        user.setUserId(userId);
//        user.setUserPw("temppassword"); //매번 db로 확인하면 효율적으로 문제가 생겨 임의로 지정
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
