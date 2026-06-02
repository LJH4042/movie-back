package com.example.back.controller;

import com.example.back.dto.movie.WatchHistoryDTO;
import com.example.back.jwt.JwtUtil;
import com.example.back.service.WatchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class WatchHistoryController {
    private final WatchHistoryService watchHistoryService;
    private final JwtUtil jwtUtil;

    //시청기록 저장
    @PostMapping("/watch/save")
    public ResponseEntity<?> saveHistory(@RequestBody WatchHistoryDTO watchHistoryDTO,
                                         @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", ""); //accessToken 가져오기

        //accessToken이 없을 경우
        if (!jwtUtil.validateToken(token)) return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        String userId = jwtUtil.getUserId(token); //accessToken에서 사용자 아이디 추출

        watchHistoryDTO.setUSER_ID(userId); //DTO에 userId 설정
        String result = watchHistoryService.saveHistory(watchHistoryDTO);

        return ResponseEntity.ok(result);
    }

    //시청기록 목록
    @GetMapping("/watch/list")
    public ResponseEntity<?> getHistory(@RequestHeader("Authorization") String authHeader,
                                        @RequestParam(defaultValue = "1") int page) {

        String token = authHeader.replace("Bearer ", ""); //accessToken 가져오기

        //accessToken이 없을 경우
        if (!jwtUtil.validateToken(token)) return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        String userId = jwtUtil.getUserId(token); //accessToken에서 사용자 아이디 추출

        return ResponseEntity.ok(watchHistoryService.getHistory(userId, page));
    }
}
