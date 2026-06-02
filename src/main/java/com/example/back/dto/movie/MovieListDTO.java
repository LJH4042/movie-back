package com.example.back.dto.movie;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MovieListDTO {
    //영화 리스트
    private List<Movie> results;

    //각 영화 정보
    @Getter
    @Setter
    public static class Movie {
        private int id; //영화 아이디
        private String title; //영화 제목
        private String poster_path; //영화 포스터
    }
}