package com.vive.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryResponse {
    private boolean success;
    private List<LotteryNumber> data;
    private String message;
    private String lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LotteryNumber {
        private int round;
        private String date;
        private List<Integer> numbers;
        private int bonus;
    }
}
