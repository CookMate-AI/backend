package com.project.cook_mate.config;

import com.project.cook_mate.jwt.JWTFilter;
import com.project.cook_mate.jwt.JWTUtil;
import com.project.cook_mate.jwt.LoginFilter;
import com.project.cook_mate.jwt.constant.SecurityConstants;
import com.project.cook_mate.user.log.LogHelper;
import com.project.cook_mate.user.repository.UserRepository;
import com.project.cook_mate.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final CorsConfig corsConfig;

    private final LogHelper logHelper;

//    @Value("${spring.security.cors.allowed-origins}")
//    private List<String> allowedOrigins;
//
    @Value("${ngrok.url}")
    private String ngrokUrl;

    @Value("${deploy.url}")
    private String deployUrl;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

//    @Bean
//    public AuthenticationManager authenticationManager() throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }


//    @Bean
//    public LoginFilter loginFilter() throws Exception {
//        LoginFilter filter = new LoginFilter(authenticationManager(), jwtUtil,userRepository, authService,logHelper);
//        filter.setFilterProcessesUrl("/users/signin");
//
//        return filter;
//    }

    @Bean
    public JWTFilter jwtAuthenticationFilter() {
        // JWTFilter의 생성자 의존성(jwtUtil, authService)은 Spring이 자동으로 채워줍니다.
        return new JWTFilter(jwtUtil, authService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

//        configuration.setAllowedOrigins(allowedOrigins);
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000",
//                                        "http://localhost:8080", ngrokUrl, deployUrl));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 CORS 설정 적용
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        LoginFilter loginFilter = new LoginFilter(
                authenticationManager(authenticationConfiguration),
                jwtUtil,
                userRepository,
                authService,
                logHelper
        );
        loginFilter.setFilterProcessesUrl("/users/signin");

        http
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 - jwt 토큰이기에 세션을 stateless 상태로 관리

                // 세션 설정 (STATELESS 유지)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인가 설정
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(SecurityConstants.Endpoints.PUBLIC).permitAll()
                        .anyRequest().authenticated()
                )

//                // ⭐️ 표준 예외 처리 핸들러 추가 (인증/인가 실패 시 응답)
//                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()) // 401 Unauthorized
//                        .accessDeniedHandler(new JwtAccessDeniedHandler())      // 403 Forbidden
//                )

                // 필터 등록 (Bean으로 등록된 필터 사용)
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new JWTFilter(jwtUtil, authService), LoginFilter.class)
//                .addFilterAfter(jwtAuthenticationFilter(), LoginFilter.class)

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
/// /--------------
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, userRepository, authService, logHelper);
//        loginFilter.setFilterProcessesUrl("/users/signin");
//
//        http
//                .cors((cors) -> cors
//                        .configurationSource(new CorsConfigurationSource() {
//                            @Override
//                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
//                                CorsConfiguration configuration = new CorsConfiguration();
//
////                                configuration.setAllowedOrigins(allowedOrigins);
//                                configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000",
//                                        "http://localhost:8080", ngrokUrl, deployUrl)); // 경로 추가시 해당 경로 뒤에 , 하고 붙이면 가능
//                                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE")); //get, post등 모든 메서드 허용
//                                configuration.setAllowCredentials(true);
//                                configuration.setAllowedHeaders(Collections.singletonList("*")); //허용할 헤더
//                                configuration.setMaxAge(3600L); //허용을 유지할 시간
//
////                                configuration.setExposedHeaders(Collections.singletonList("Authorization")); //Authorization헤더 허용
//                                configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie"));
//
//                                return configuration;
//                            }
//                        }));
//
//        http
//                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 - jwt 토큰이기에 세션을 stateless 상태로 관리
//
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/users/signUp").permitAll() // 특정 경로 허용
//                        .requestMatchers(HttpMethod.POST, "/users/signup").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/users/check-id").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/users/check-Email/send-Email").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/users/check-Email/certification").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/users/check-nickname").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/users/signin").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/users/find-id/send-Email").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/users/find-id/certification").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/users/find-pw").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/reissue").permitAll()
//
//                                .requestMatchers(HttpMethod.POST, "/recipe/menu").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/recipe/recommend").permitAll()
//
//
//                        .requestMatchers(HttpMethod.GET, "/users/test").hasAuthority("user") //없어도 anyRequest로 인하여 인증 절차 거침
//
//                        .anyRequest().authenticated()
//                )
//
//                .formLogin(form -> form.disable()) // Form Login 비활성화
//                .httpBasic(httpBasic -> httpBasic.disable()); // Basic Auth 비활성화
//
//        http
//                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
//
//        http
//                .addFilterAfter(new JWTFilter(jwtUtil, authService), LoginFilter.class);
//        //세션설정
//        http
//                .sessionManagement((session) -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//
//        return http.build();
//    }

}
