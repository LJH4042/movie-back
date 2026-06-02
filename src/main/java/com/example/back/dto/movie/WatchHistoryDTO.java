package com.example.back.dto.movie;

import com.example.back.entity.UserEntity;
import com.example.back.entity.WatchHistoryEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WatchHistoryDTO {
    private String USER_ID; //유저 아이디
    private int MOVIE_ID; //영화 아이디
    private String TITLE; //영화 제목
    private String POSTER_PATH; //영화 포스터
    private String GENRES; //영화 장르

    //DTO → Entity 변환
    public WatchHistoryEntity toEntity() {
        return new WatchHistoryEntity(USER_ID, MOVIE_ID, TITLE, POSTER_PATH, GENRES);
    }
}
