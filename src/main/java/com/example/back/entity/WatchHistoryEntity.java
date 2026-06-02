package com.example.back.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
public class WatchHistoryEntity {
    private Long HISTORY_NO; //시청기록 고유번호
    private String USER_ID; //유저 아이디
    private int MOVIE_ID; //영화 아이디
    private String TITLE; //영화 제목
    private String POSTER_PATH; //영화 포스터
    private String GENRES; //영화 장르
    private Date WATCH_DATE; //시청 날짜

    //시청기록 저장
    public WatchHistoryEntity(String USER_ID, int MOVIE_ID, String TITLE, String POSTER_PATH, String GENRES) {
        this.USER_ID = USER_ID;
        this.MOVIE_ID = MOVIE_ID;
        this.TITLE = TITLE;
        this.POSTER_PATH = POSTER_PATH;
        this.GENRES = GENRES;
    }
}
