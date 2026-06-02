package com.example.back.jwt;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey; //application.properties에 비밀키 가져오기

    @Value("${jwt.accessToken-expiration}")
    private long accessTokenExpiration; //application.properties에 accessToken 만료시간 가져오기

    @Value("${jwt.refreshToken-expiration}")
    private long refreshTokenExpiration; //application.properties에 refreshToken 만료시간 가져오기

    private SecretKey key; //JWT 서명에 사용할 Key 객체

    //비밀키를 암호화 Key 객체로 생성
    //객체 생성 후 한 번만 실행되는 초기화 메서드
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    //토큰 생성 함수
    private String createTokenFunction(String userId, long expirationTime, String type) {
        return Jwts.builder()
                .subject(userId) //토큰 사용자
                .issuedAt(new Date()) //토큰 발급시간
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) //토큰 만료시간
                .claim("type", type) //토큰 타입
                .signWith(key) //암호화 키로 토큰 생성
                .compact(); //토큰 문자열 생성
    }

    //accessToken 생성
    public String createAccessToken(String userId) {
        return createTokenFunction(userId, accessTokenExpiration, "access");
    }

    //refreshToken 생성
    public String createRefreshToken(String userId) {
        return createTokenFunction(userId, refreshTokenExpiration, "refresh");
    }

    //JWT Parser 생성
    private JwtParser getParser() {
        return Jwts.parser()
                .verifyWith(key) //토큰 검증
                .build(); //토큰을 사용할 수 있게 객체로 생성
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            getParser().parseSignedClaims(token); //토큰 해석
            return true;
        } catch (Exception e) {
            return false; //토큰 관련 에러 처리
        }
    }

    //토큰에서 userId 추출
    public String getUserId(String token) {
        return getParser()
                .parseSignedClaims(token) //토큰 해석
                .getPayload() //payload 꺼내기
                .getSubject(); //userId 반환
    }
}