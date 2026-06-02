package com.example.back.dto.user;

import com.example.back.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDTO {
    private String USER_ID; //유저 아이디
    private String USER_PW; //유저 비밀번호
    private String USER_EMAIL; //유저 이메일

    // DTO → Entity 변환
    public UserEntity toEntity() {
        return new UserEntity(USER_ID, USER_PW, USER_EMAIL);
    }
}
