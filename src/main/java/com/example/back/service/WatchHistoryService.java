package com.example.back.service;

import com.example.back.dto.movie.WatchHistoryDTO;
import com.example.back.entity.WatchHistoryEntity;
import com.example.back.mapper.WatchHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WatchHistoryService {
    private final WatchHistoryMapper watchHistoryMapper;

    //시청기록 저장
    public String saveHistory(WatchHistoryDTO watchHistoryDTO) {
        watchHistoryMapper.saveHistory(watchHistoryDTO.toEntity());
        return "시청기록 저장";
    }

    //시청기록 목록
    public Map<String, Object> getHistory(String userId, int page) {
        //mapper.xml에 전달할 파라미터(유저 아이디, 페이지, 시청기록 개수)
        List<WatchHistoryEntity> list = watchHistoryMapper.historyList(
                Map.of("USER_ID", userId, "offset", (page - 1) * 20, "size", 20)
        );

        int total = watchHistoryMapper.countList(userId); //시청기록 총 개수

        //프론트로 시청기록 목록, 시청기록 개수 보내기
        return new HashMap<>(
                Map.of("list", list, "total", total)
        );
    }

    //많이 본 장르 추출
    public String getManyGenres(String userId) {
        int total = watchHistoryMapper.countList(userId); //시청기록 총 개수

        List<WatchHistoryEntity> list = watchHistoryMapper.historyList(
                Map.of("USER_ID", userId, "offset", 0, "size", total)
        );

        Map<String, Integer> genreCount = new HashMap<>();  //장르별 시청 횟수를 저장할 Map

        //시청기록 데이터 하나씩 순회
        for (WatchHistoryEntity item : list) {
            String[] genres = item.getGENRES().split(","); //장르 id 분리

            //각 장르를 하나씩 처리
            for (String g : genres) {
                genreCount.put(g, genreCount.getOrDefault(g, 0) + 1); //해당 장르의 카운트 증가
            }
        }

        //장르를 많이 본 순으로 정렬 후 상위 3개 추출
        return genreCount.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue()) //내림차순 정렬 (많이 본 순)
                .limit(3).map(Map.Entry::getKey) //상위 3개 장르
                .reduce((a, b) -> a + "," + b).orElse(""); //장르 id를 문자열로 결합
    }

    //이미 시청기록에 있는 영화 ID 리스트
    public List<Integer> getWatchedMovieList(String userId) {
        int total = watchHistoryMapper.countList(userId); //시청기록 총 개수

        return watchHistoryMapper.historyList(
                        Map.of("USER_ID", userId, "offset", 0, "size", total)
                ).stream()
                .map(WatchHistoryEntity::getMOVIE_ID)
                .toList();
    }
}
