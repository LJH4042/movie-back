package com.example.back.service;

import com.example.back.dto.user.LoginDTO;
import com.example.back.dto.user.ProfileDTO;
import com.example.back.dto.user.RegisterDTO;
import com.example.back.entity.UserEntity;
import com.example.back.mapper.UserMapper;
import com.example.back.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    //회원가입
    public String register(RegisterDTO registerDTO){
        // 아이디 중복 체크
        if (userMapper.findByUserId(registerDTO.getUSER_ID()) != null) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        //비밀번호를 bcrypt 암호화
        String encodedPw = passwordEncoder.encode(registerDTO.getUSER_PW());
        registerDTO.setUSER_PW(encodedPw);

        userMapper.register(registerDTO.toEntity()); //DTO를 Entity로 변환

        return "회원가입 성공";
    }

    //로그인 회원 검증
    public Map<String, String> login(LoginDTO loginDTO) {
        //DTO를 Entity로 변환
        UserEntity user = userMapper.findByUserId(loginDTO.getUSER_ID());

        //아이디 존재 여부
        if (user == null) throw new RuntimeException("존재하지 않는 아이디입니다.");

        //아이디 존재 확인, 입력한 비밀번호와 DB에 저장된 암호화된 비밀번호 비교
        //비밀번호가 틀렸을 경우
        if (!passwordEncoder.matches(loginDTO.getUSER_PW(), user.getUSER_PW())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(user.getUSER_ID()); //accessToken 생성
        String refreshToken = jwtUtil.createRefreshToken(user.getUSER_ID()); //refreshToken 생성

        //Redis에 refreshToken 7일 저장(추가)
        redisService.saveRefreshToken(user.getUSER_ID(), refreshToken, 7 * 24 * 60 * 60);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    //사용자 프로필 가져오기
    public ProfileDTO getProfile(String userId) {
        UserEntity user = userMapper.findByUserId(userId); //사용자 아이디 확인

        if (user == null) throw new RuntimeException("사용자를 찾을 수 없습니다.");

        return new ProfileDTO(user.getUSER_ID(), user.getUSER_EMAIL(), user.getREG_DATE());
    }
}
