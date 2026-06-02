package com.example.back.mapper;

import com.example.back.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    //회원가입
    void register (UserEntity userEntity);

    //로그인 회원 검증
    UserEntity findByUserId(String userId);
}
