package com.vive.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.LocalDateTime;
import java.util.List;

@RedisHash("WeeklyRecommendation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyRecommendation {

    @Id
    private String id; // "weekly:2024-W01"

    private String weekKey; // "2024-W01"

    private LocalDateTime generatedAt;

    private List<RecommendationSet> recommendations; // 5개의 추천 번호 세트

    @TimeToLive
    @Builder.Default
    private Long ttl = 1209600L; // 2주 (초 단위)

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationSet {
        private List<Integer> numbers; // 6개 메인 번호
        private Integer bonusNumber;
    }
}
