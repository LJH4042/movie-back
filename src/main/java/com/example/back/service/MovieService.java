package com.example.back.service;

import com.example.back.dto.movie.MovieDetailDTO;
import com.example.back.dto.movie.MovieListDTO;
import com.example.back.dto.movie.MovieVideoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final RestClient restClient;
    private final WatchHistoryService watchHistoryService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${tmdb.api.key}")
    private String tmdbKey; //API Key

    //영화 리스트 가져오기
    public MovieListDTO getMoviesList(int page) {
        long startTime = System.currentTimeMillis();

        String key = "movie:popular:" + page; //캐시 키

        //redis에 저장된 캐시가 있으면 가져오기
        String cached = redisService.getMovieInfo(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, MovieListDTO.class);
            } catch (Exception e) {
                log.error("Redis 캐시 JSON 파싱 실패", e);
            }
        }

        //영화 리스트
        MovieListDTO result = restClient.get()
                .uri(uriBuilder -> uriBuilder //URL을 빌드
                        .path("/movie/popular") //TMDB API 경로 설정
                        .queryParam("api_key", tmdbKey).queryParam("language", "ko-KR") //인증키, 한국어로 설정
                        .queryParam("page", page).build()) //페이지 번호(프론트엔드에서 받기)
                .retrieve() //메서드 체인 시작
                .body(MovieListDTO.class); //body에 담아 MovieListDTO 객체로 매핑

        //Redis 캐시에 저장(유효기간 30분)
        try {
            redisService.saveMovieInfo(key, objectMapper.writeValueAsString(result), 1800);
        } catch (Exception e) {
            log.error("Redis 저장 실패", e);
        }

        return result;
    }

    //영화 상세 정보 가져오기
    public MovieDetailDTO getMovieDetail(int id) {
        String key = "movie:detail:" + id; //캐시 키

        //redis에 저장된 캐시가 있으면 가져오기
        String cached = redisService.getMovieInfo(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, MovieDetailDTO.class);
            } catch (Exception e) {
                log.error("Redis 캐시 JSON 파싱 실패", e);
            }
        }

        //영화 상세 정보
        MovieDetailDTO movie = restClient.get()
                .uri(uriBuilder -> uriBuilder //URL을 빌드
                        .path("/movie/{movie_id}") //TMDB API 경로 설정
                        .queryParam("api_key", tmdbKey).queryParam("language", "ko-KR") //인증키, 한국어로 설정
                        .build(id)) //프론트로부터 영화 아이디를 받아 빌드
                .retrieve() //메서드 체인 시작
                .body(MovieDetailDTO.class); //body에 담아 MovieDetailDTO 객체로 매핑

        //배우, 제작진 정보 가져오기
        MovieDetailDTO credits = restClient.get()
                .uri(uriBuilder -> uriBuilder //URL을 빌드
                        .path("/movie/{movie_id}/credits") //TMDB API 경로 설정
                        .queryParam("api_key", tmdbKey).queryParam("language", "ko-KR") //인증키, 한국어로 설정
                        .build(id)) //프론트로부터 영화 아이디를 받아 빌드
                .retrieve() //메서드 체인 시작
                .body(MovieDetailDTO.class); //body에 담아 MovieDetailDTO 객체로 매핑

        //배우 5명만 가져오기
        List<MovieDetailDTO.Cast> topCast = Objects.requireNonNull(credits).getCast().stream()
                .limit(7).toList(); //5개만 리스트에 넣기

        Objects.requireNonNull(movie).setCast(topCast);

        //제작진 중에서 감독만 가져오기
        List<MovieDetailDTO.Crew> directors = credits.getCrew().stream()
                .filter(crew -> "Director".equals(crew.getJob())).toList(); //Director만 리스트에 넣기

        movie.setCrew(directors);

        //Redis 캐시에 저장(유효기간 10분)
        try {
            redisService.saveMovieInfo(key, objectMapper.writeValueAsString(movie), 600);
        } catch (Exception e) {
            log.error("Redis 저장 실패", e);
        }

        return movie;
    }

    //예고편 가져오기
    public String getMovieTrailer(int id) {
        String key = "movie:trailer:" + id; //캐시 키

        //redis에 저장된 캐시가 있으면 가져오기
        String cached = redisService.getMovieInfo(key);
        if (cached != null) return cached;

        MovieVideoDTO response = restClient.get()
                .uri(uriBuilder -> uriBuilder //URL을 빌드
                        .path("/movie/{movie_id}/videos").queryParam("api_key", tmdbKey) //인증키, 한국어로 설정
                        .build(id)) //프론트로부터 영화 아이디를 받아 빌드
                .retrieve() //메서드 체인 시작
                .body(MovieVideoDTO.class); //body에 담아 VideoDTO 객체로 매핑

        //예고편 리스트를 stream으로 변환
        String result = Objects.requireNonNull(response).getResults().stream()
                .filter(v -> "YouTube".equals(v.getSite()) && "Trailer".equals(v.getType())) //"YouTube"에 올라온 예고편 가져오기
                .map(MovieVideoDTO.VideoResult::getKey) //각 예고편의 key 추출
                .findFirst() //첫번째 예고편을 보여줌
                .orElse(null); //예고편이 없으면 null 반환

        redisService.saveMovieInfo(key, result, 600); //Redis 캐시에 저장(유효기간 10분)

        return result;
    }

    //영화 검색
    public MovieListDTO searchMovies(String query) {
        String key = "movie:search:" + query; //캐시 키

        //redis에 저장된 캐시가 있으면 가져오기
        String cached = redisService.getMovieInfo(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, MovieListDTO.class);
            } catch (Exception e) {
                log.error("Redis 캐시 JSON 파싱 실패", e);
            }
        }

        MovieListDTO result = restClient.get() //get 요청
                .uri(uriBuilder -> uriBuilder //URL을 빌드
                        .path("/search/movie") //TMDB API 경로 설정
                        .queryParam("api_key", tmdbKey).queryParam("language", "ko-KR") //인증키, 한국어로 설정
                        .queryParam("query", query).build()) //쿼리 번호(프론트엔드에서 받기)
                .retrieve() //메서드 체인 시작
                .body(MovieListDTO.class); //body에 담아 MovieListDTO 객체로 매핑

        //Redis 캐시에 저장(유효기간 10분)
        try {
            redisService.saveMovieInfo(key, objectMapper.writeValueAsString(result), 600);
        } catch (Exception e) {
            log.error("Redis 저장 실패", e);
        }

        return result;
    }

    //영화 추천
    public MovieListDTO recommendMovies(String userId) {
        String key = "movie:recommend:" + userId; //캐시 키

        //redis에 저장된 캐시가 있으면 가져오기
        String cached = redisService.getMovieInfo(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, MovieListDTO.class);
            } catch (Exception e) {
                log.error("Redis 캐시 JSON 파싱 실패", e);
            }
        }

        String genres = watchHistoryService.getManyGenres(userId); //가장 많은 장르

        if (genres == null || genres.isBlank()) return new MovieListDTO(); //시청 기록이 없으면 빈 값 반환

        List<Integer> watchedList = new ArrayList<>(watchHistoryService.getWatchedMovieList(userId)); //이미 시청기록에 있는 영화 리스트

        List<MovieListDTO.Movie> recommendList = new ArrayList<>(); //프론트로 보낼 추천 영화 리스트

        int page = 1; //tmdb 페이지

        //최대 3페이지까지, 추천 영화 리스트 20개 이상이 될때까지
        while (recommendList.size() <= 20 && page <= 3) {
            int insertPage = page;

            //영화 추천
            MovieListDTO response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/discover/movie") //TMDB API 경로 설정
                            .queryParam("api_key", tmdbKey).queryParam("language", "ko-KR") //인증키, 한국어로 설정
                            .queryParam("sort_by", "popularity.desc") //인기순 정렬
                            .queryParam("with_genres", genres) //사용자가 선호하는 장르
                            .queryParam("page", String.valueOf(insertPage)) //영화 페이지
                            .build())
                .retrieve() //메서드 체인 시작
                .body(MovieListDTO.class); //body에 담아 MovieListDTO 객체로 매핑

            if (response == null || response.getResults() == null) break; //응답이 없으면 반복 종료

            //이미 시청기록에 있는 영화 제거
            List<MovieListDTO.Movie> filtered = response.getResults().stream()
                    .filter(movie -> !watchedList.contains(movie.getId()))
                    .filter(movie -> recommendList.stream().noneMatch(m -> m.getId() == movie.getId()))
                    .toList();

            recommendList.addAll(filtered); //시청기록에 있는 영화 제거 후 추천 영화 리스트에 삽입

            page++; //다음 페이지로 넘어가기
        }

        List<MovieListDTO.Movie> resultList = recommendList.stream().limit(20).toList(); //최대 20개로 제한

        MovieListDTO result = new MovieListDTO();
        result.setResults(resultList);

        //Redis 캐시에 저장(유효기간 10분)
        try {
            redisService.saveMovieInfo(key, objectMapper.writeValueAsString(result), 600);
        } catch (Exception e) {
            log.error("Redis 저장 실패", e);
        }

        return result;
    }
}