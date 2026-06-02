package com.example.back.mapper;

import com.example.back.entity.WatchHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface WatchHistoryMapper {
    //시청기록 저장
    void saveHistory (WatchHistoryEntity watchHistoryEntity);

    //시청기록 목록
    List<WatchHistoryEntity> historyList(Map<String, Object> param);

    //시청기록 개수
    int countList(String userId);
}
