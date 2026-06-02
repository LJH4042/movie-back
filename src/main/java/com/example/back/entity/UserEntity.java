package com.example.back.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
public class UserEntity {
    private Long USER_NO; //유저 번호
    private String USER_ID; //유저 아이디
    private String USER_PW; //유저 비밀번호
    private String USER_EMAIL; //유저 이메일
    private Date REG_DATE; //유저 가입날짜

    //회원가입 생성자
    public UserEntity(String USER_ID, String USER_PW, String USER_EMAIL) {
        this.USER_ID = USER_ID;
        this.USER_PW = USER_PW;
        this.USER_EMAIL = USER_EMAIL;
    }

    //로그인 회원 검증 생성자
    public UserEntity(String USER_ID, String USER_PW) {
        this.USER_ID = USER_ID;
        this.USER_PW = USER_PW;
    }
}
