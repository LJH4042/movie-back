package com.example.back.controller;

import com.example.back.dto.user.LoginDTO;
import com.example.back.dto.user.RegisterDTO;
import com.example.back.dto.user.TokenDTO;
import com.example.back.jwt.JwtUtil;
import com.example.back.service.RedisService;
import com.example.back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RedisService redisService;
    private final JwtUtil jwtUtil;

    //회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO registerDTO){
        try {
            String result = userService.register(registerDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //로그인 회원 검증
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            Map<String, String> tokens = userService.login(loginDTO);

            //RefreshToken을 쿠키로 생성
            ResponseCookie insertCookie = ResponseCookie.from("refreshToken", tokens.get("refreshToken"))
                    .httpOnly(true).path("/") //JS에서 접근 불가 (XSS 방어), 모든 경로에서 쿠키 사용 가능
                    .maxAge(7 * 24 * 60 * 60).build(); //7일 동안 유지, 쿠키 객체 생성

            return ResponseEntity.ok() //응답 성공
                    .header("Set-Cookie", insertCookie.toString()) //Set-Cookie 헤더에 RefreshToken 담아서 브라우저에 저장
                    .body(new TokenDTO(true, tokens.get("accessToken"), "로그인에 성공하였습니다.")); //body에 AccessToken 반환
        } catch (Exception e) {
            return ResponseEntity.badRequest() //로그인 실패 시 에러 응답
                    .body(new TokenDTO(false, null, e.getMessage()));
        }
    }

    //RefreshToken로 accessToken 재발급
    @PostMapping("/reToken")
    public ResponseEntity<?> reToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {

        //RefreshToken이 없거나 만료된 경우, 쿠키에 저장된 RefreshToken 삭제
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true).path("/").maxAge(0).build();

            return ResponseEntity.status(401)
                    .header("Set-Cookie", deleteCookie.toString())
                    .body(new TokenDTO(false, null, "Refresh Token이 없거나 만료되었습니다"));
        }

        String userId = jwtUtil.getUserId(refreshToken); //RefreshToken 안에 저장된 userId 꺼냄
        String savedToken = redisService.getRefreshToken(userId); //Redis에 저장된 RefreshToken와 비교

        //RefreshToken이 없거나 쿠키-redis의 저장된 RefreshToken이 다를 경우, 쿠키에 저장된 RefreshToken 삭제
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true).path("/").maxAge(0).build();

            return ResponseEntity.status(403)
                    .header("Set-Cookie", deleteCookie.toString())
                    .body(new TokenDTO(false, null, "다시 로그인해주세요"));
        }

        redisService.deleteRefreshToken(userId); //redis에 있던 기존 RefreshToken 삭제

        String newAccessToken = jwtUtil.createAccessToken(userId);  //새 AccessToken 토큰 발급
        String newRefreshToken = jwtUtil.createRefreshToken(userId); //새 RefreshToken 토큰 발급

        redisService.saveRefreshToken(userId, newRefreshToken, 7 * 24 * 60 * 60); //새 RefreshToken redis에 저장

        //쿠키에 RefreshToken 교체
        ResponseCookie newCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true).path("/").maxAge(7 * 24 * 60 * 60).build();

        return ResponseEntity.ok()
                .header("Set-Cookie", newCookie.toString())
                .body(new TokenDTO(true, newAccessToken, "토큰 재발급 성공")); //새 AccessToken 발급
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue("refreshToken") String refreshToken) {

        String userId = jwtUtil.getUserId(refreshToken); //RefreshToken 안에 저장된 userId 꺼냄

        redisService.deleteRefreshToken(userId); //redis에 있던 기존 RefreshToken 삭제

        //쿠키에 저장된 refreshToken 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).path("/").maxAge(0).build();

        return ResponseEntity.ok()
                .header("Set-Cookie", deleteCookie.toString()).body("로그아웃 완료"); //응답 보냄
    }

    //사용자 프로필
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", ""); //accessToken 가져오기

            //accessToken이 없을 경우
            if (!jwtUtil.validateToken(token)) return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");

            String userId = jwtUtil.getUserId(token); //accessToken에서 사용자 아이디 추출

            return ResponseEntity.ok(userService.getProfile(userId)); //사용자 프로필 보내기
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage()); //에러 메시지 보내기
        }
    }
}
