package com.example.back.dto.user;

import com.example.back.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    private String USER_ID; //유저 아이디
    private String USER_PW; //유저 비밀번호

    // DTO → Entity 변환
    public UserEntity toEntity() {
        return new UserEntity(USER_ID, USER_PW);
    }
}
