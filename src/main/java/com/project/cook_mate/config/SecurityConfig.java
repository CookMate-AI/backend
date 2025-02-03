package com.project.cook_mate.config;

import com.project.cook_mate.jwt.JWTFilter;
import com.project.cook_mate.jwt.JWTUtil;
import com.project.cook_mate.jwt.LoginFilter;
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
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

        http
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        //세션설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}
