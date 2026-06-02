package com.example.back.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenDTO {
    private boolean success; //성공 여부
    private String accessToken; //토큰
    private String message; //전송 메시지
}
