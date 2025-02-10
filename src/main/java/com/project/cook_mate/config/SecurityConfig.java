package com.project.cook_mate.config;

import com.project.cook_mate.jwt.JWTFilter;
import com.project.cook_mate.jwt.JWTUtil;
import com.project.cook_mate.jwt.LoginFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);
        loginFilter.setFilterProcessesUrl("/users/signin");

        http
                .cors((cors) -> cors
                        .configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                CorsConfiguration configuration = new CorsConfiguration();

                                configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // 경로 추가시 해당 경로 뒤에 , 하고 붙이면 가능
                                configuration.setAllowedMethods(Collections.singletonList("*")); //get, post등 모든 메서드 허용
                                configuration.setAllowCredentials(true);
                                configuration.setAllowedHeaders(Collections.singletonList("*")); //허용할 헤더
                                configuration.setMaxAge(3600L); //허용을 유지할 시간

                                configuration.setExposedHeaders(Collections.singletonList("Authorization")); //Authorization헤더 허용

                                return configuration;
                            }
                        }));

        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 - jwt 토큰이기에 세션을 stateless 상태로 관리

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/signUp").permitAll() // 특정 경로 허용
                        .requestMatchers(HttpMethod.POST, "/users/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/check-id/{user_id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/check-Email/send-Email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/check-Email/certification").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/check-Nname/{nickName}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/signin").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/find-id/send-Email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/find-id/certification").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/find-pw").permitAll()

                        .requestMatchers(HttpMethod.GET, "/users/test").hasAuthority("user")

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form.disable()) // Form Login 비활성화
                .httpBasic(httpBasic -> httpBasic.disable()); // Basic Auth 비활성화

        http
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        http
                .addFilterAfter(new JWTFilter(jwtUtil), LoginFilter.class);
        //세션설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}
