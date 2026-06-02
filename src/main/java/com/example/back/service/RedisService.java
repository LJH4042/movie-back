package com.example.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    //Redis에 refreshToken 저장
    public void saveRefreshToken(String userId, String refreshToken, long expiration) {
        redisTemplate.opsForValue().set(userId, refreshToken, expiration, TimeUnit.SECONDS);
    }

    //Redis에 refreshToken 조회
    public String getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get(userId);
    }

    //Redis에 refreshToken 삭제
    public void deleteRefreshToken(String userId) {
        redisTemplate.delete(userId);
    }


    //Redis에 영화 정보 저장
    public void saveMovieInfo(String key, String movieInfo, long expiration) {
        redisTemplate.opsForValue().set(key, movieInfo, expiration, TimeUnit.SECONDS);
    }

    //Redis에 영화 정보 조회
    public String getMovieInfo(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}