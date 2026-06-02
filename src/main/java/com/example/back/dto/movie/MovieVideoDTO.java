package com.example.back.dto.movie;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MovieVideoDTO {

    private List<VideoResult> results;

    @Getter
    @Setter
    public static class VideoResult {
        private String key; //예고편 key
        private String site; //예고편 제공 사이트
        private String type; //예고편 유형
    }
}