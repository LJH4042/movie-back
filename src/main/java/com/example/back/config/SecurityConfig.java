package com.example.back.config;

import com.example.back.jwt.JwtFilter;
import com.example.back.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    // JwtUtil 주입
    private final JwtUtil jwtUtil;

    //필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) //CSRF 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) //CORS 적용
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //세션 비활성화
                .authorizeHttpRequests(auth -> auth  //요청 권한 설정 (누가 어떤 api에 접근 가능한지)
                        .requestMatchers("/api/register", "/api/login", "/api/reToken", "/api/logout").permitAll() //비회원도 접근 가능
                        .anyRequest().permitAll())
                .addFilterBefore( new JwtFilter(jwtUtil), // JWT 필터
                        UsernamePasswordAuthenticationFilter.class //이 필터 전에 실행
                ).logout(AbstractHttpConfigurer::disable) //Spring Security에서 로그아웃 처리를 하지 못하도록 비활성화
        ;

        return http.build();
    }

    //CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("https://movie-front.vercel.app")); //cors를 허용할 프론트 주소
//        config.setAllowedOrigins(List.of("http://localhost:5173")); //cors를 허용할 프론트 주소
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); //cors를 허용할 HTTP 메소드 (모두 허용으로 설정)
        config.setAllowedHeaders(List.of("*")); //cors를 하용할 클라이언트 헤더 (모두 허용으로 설정)
        config.setAllowCredentials(true);

        //cors를 적용할 api (모든 api로 설정)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    //Bcrypt 사용
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
