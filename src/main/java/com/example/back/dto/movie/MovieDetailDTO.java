package com.example.back.dto.movie;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MovieDetailDTO {

    private int id; //영화 아이디
    private String title; //영화 제목
    private String overview; //영화 줄거리
    private String poster_path; //영화 포스터 경로
    private String release_date; //영화 개봉일
    private double vote_average; //영화 평점

    private List<Genre> genres; //영화 장르 리스트
    private List<Cast> cast; //영화 배우 리스트
    private List<Crew> crew; //영화 제작진 리스트

    @Getter
    @Setter
    public static class Genre {
        private int id; //장르 아이디
        private String name; //장르 이름
    }

    @Getter
    @Setter
    public static class Cast {
        private int id; //배우 아이디
        private String name; //배우 이름
        private String profile_path; //배우 이미지 경로
    }

    @Getter
    @Setter
    public static class Crew {
        private int id; //제작진 아이디
        private String name; //제작진 이름
        private String job; //제작진 직책
    }
}