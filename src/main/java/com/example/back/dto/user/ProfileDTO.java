package com.example.back.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class ProfileDTO {
    private String USER_ID; //유저 아이디
    private String USER_EMAIL; //유저 이메일
    private Date REG_DATE; //유저 가입날짜
}
