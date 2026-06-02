package com.example.back.controller;

import com.example.back.jwt.JwtUtil;
import com.example.back.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final JwtUtil jwtUtil;

    //영화 리스트 가져오기
    @GetMapping("/movieList")
    public ResponseEntity<?> getNowPlaying(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(movieService.getMoviesList(page));
    }

    //영화 상세 가져오기
    @GetMapping("/movie/{id}")
    public ResponseEntity<?> getMovieDetail(@PathVariable int id) {
        return ResponseEntity.ok(movieService.getMovieDetail(id));
    }

    //예고편 가져오기
    @GetMapping("/movie/{id}/trailer")
    public ResponseEntity<?> getTrailer(@PathVariable int id) {
        return ResponseEntity.ok(movieService.getMovieTrailer(id));
    }

    //영화 검색
    @GetMapping("/movie/search")
    public ResponseEntity<?> searchMovies(@RequestParam String query) {
        return ResponseEntity.ok(movieService.searchMovies(query));
    }

    //영화 추천
    @GetMapping("/movie/recommend")
    public ResponseEntity<?> recommend(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", ""); //accessToken 가져오기

        //accessToken이 없을 경우
        if (!jwtUtil.validateToken(token)) return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        String userId = jwtUtil.getUserId(token); //accessToken에서 사용자 아이디 추출

        return ResponseEntity.ok(movieService.recommendMovies(userId));
    }
}