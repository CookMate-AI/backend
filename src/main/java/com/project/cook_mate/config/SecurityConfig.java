package com.project.cook_mate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화

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
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form.disable()) // Form Login 비활성화
                .httpBasic(httpBasic -> httpBasic.disable()); // Basic Auth 비활성화

        return http.build();
    }

}
