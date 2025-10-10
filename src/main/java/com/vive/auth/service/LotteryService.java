package com.vive.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vive.auth.dto.LotteryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String LOTTERY_API_URL = "https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=%d";
    private static final LocalDate FIRST_DRAW_DATE = LocalDate.of(2002, 12, 7);

    /**
     * 최근 5개 로또 당첨번호 조회 (캐싱)
     */
    @Cacheable(value = "lotteryNumbers", key = "'recent'")
    public LotteryResponse getRecentLotteryNumbers() {
        log.info("Fetching recent lottery numbers from API...");

        try {
            int estimatedCurrentRound = getCurrentEstimatedRound();
            List<LotteryResponse.LotteryNumber> results = new ArrayList<>();

            // 최대 10회까지 역순으로 시도해서 최근 5개 찾기
            int foundCount = 0;
            for (int i = 0; i < 10 && foundCount < 5; i++) {
                int round = estimatedCurrentRound - i;
                LotteryResponse.LotteryNumber lotteryData = fetchLotteryNumber(round);

                if (lotteryData != null) {
                    results.add(lotteryData);
                    foundCount++;
                }
            }

            if (results.isEmpty()) {
                log.warn("No lottery data found from API, using fallback data");
                return LotteryResponse.builder()
                        .success(false)
                        .data(getFallbackData())
                        .message("API 데이터를 가져올 수 없어 fallback 데이터를 사용합니다.")
                        .lastUpdated(LocalDateTime.now().toString())
                        .build();
            }

            log.info("Successfully fetched {} lottery numbers", results.size());
            return LotteryResponse.builder()
                    .success(true)
                    .data(results)
                    .lastUpdated(LocalDateTime.now().toString())
                    .build();

        } catch (Exception e) {
            log.error("Error fetching lottery numbers", e);
            return LotteryResponse.builder()
                    .success(false)
                    .data(getFallbackData())
                    .message("서버 오류로 인해 fallback 데이터를 사용합니다.")
                    .lastUpdated(LocalDateTime.now().toString())
                    .build();
        }
    }

    /**
     * 특정 회차 로또 번호 조회
     */
    private LotteryResponse.LotteryNumber fetchLotteryNumber(int round) {
        try {
            String url = String.format(LOTTERY_API_URL, round);
            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(response);

            // API 응답 성공 확인
            String returnValue = jsonNode.path("returnValue").asText();
            if (!"success".equals(returnValue)) {
                return null;
            }

            // 번호 추출 및 정렬
            List<Integer> numbers = Arrays.asList(
                jsonNode.path("drwtNo1").asInt(),
                jsonNode.path("drwtNo2").asInt(),
                jsonNode.path("drwtNo3").asInt(),
                jsonNode.path("drwtNo4").asInt(),
                jsonNode.path("drwtNo5").asInt(),
                jsonNode.path("drwtNo6").asInt()
            ).stream().sorted().collect(Collectors.toList());

            String date = jsonNode.path("drwNoDate").asText().replace("-", ".");
            int bonus = jsonNode.path("bnusNo").asInt();

            return LotteryResponse.LotteryNumber.builder()
                    .round(round)
                    .date(date)
                    .numbers(numbers)
                    .bonus(bonus)
                    .build();

        } catch (Exception e) {
            log.debug("Failed to fetch lottery number for round {}: {}", round, e.getMessage());
            return null;
        }
    }

    /**
     * 현재 추정 회차 계산
     */
    private int getCurrentEstimatedRound() {
        LocalDate now = LocalDate.now();
        long daysDiff = ChronoUnit.DAYS.between(FIRST_DRAW_DATE, now);
        long weeksDiff = daysDiff / 7;
        return (int) (weeksDiff + 1);
    }

    /**
     * Fallback 데이터
     */
    private List<LotteryResponse.LotteryNumber> getFallbackData() {
        return Arrays.asList(
            LotteryResponse.LotteryNumber.builder()
                .round(1154).date("2024.03.23")
                .numbers(Arrays.asList(1, 5, 11, 16, 20, 27)).bonus(31).build(),
            LotteryResponse.LotteryNumber.builder()
                .round(1153).date("2024.03.16")
                .numbers(Arrays.asList(2, 6, 12, 15, 30, 44)).bonus(9).build(),
            LotteryResponse.LotteryNumber.builder()
                .round(1152).date("2024.03.09")
                .numbers(Arrays.asList(3, 8, 13, 19, 28, 42)).bonus(25).build(),
            LotteryResponse.LotteryNumber.builder()
                .round(1151).date("2024.03.02")
                .numbers(Arrays.asList(7, 11, 17, 22, 35, 40)).bonus(14).build(),
            LotteryResponse.LotteryNumber.builder()
                .round(1150).date("2024.02.24")
                .numbers(Arrays.asList(4, 9, 18, 24, 32, 45)).bonus(21).build()
        );
    }
}
