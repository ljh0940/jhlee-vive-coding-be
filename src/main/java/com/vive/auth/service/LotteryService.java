package com.vive.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vive.auth.dto.LotteryResponse;
import com.vive.auth.entity.LotteryNumber;
import com.vive.auth.repository.LotteryNumberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryService {

    private final LotteryNumberRepository lotteryNumberRepository;
    private final com.vive.auth.repository.WeeklyRecommendationRepository weeklyRecommendationRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String LOTTERY_API_URL = "https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=%d";
    private static final LocalDate FIRST_DRAW_DATE = LocalDate.of(2002, 12, 7);

    /**
     * 최근 5개 로또 당첨번호 조회 (Redis에서)
     */
    public LotteryResponse getRecentLotteryNumbers() {
        log.info("Fetching recent lottery numbers from Redis...");

        try {
            List<LotteryNumber> allNumbers = lotteryNumberRepository.findAll();

            if (allNumbers.isEmpty()) {
                log.warn("No lottery data found in Redis, using fallback data");
                return LotteryResponse.builder()
                        .success(false)
                        .data(getFallbackData())
                        .message("저장된 데이터가 없습니다. fallback 데이터를 사용합니다.")
                        .lastUpdated(LocalDateTime.now().toString())
                        .build();
            }

            // round로 정렬하고 최근 5개만 가져오기
            List<LotteryResponse.LotteryNumber> results = allNumbers.stream()
                    .sorted((a, b) -> b.getRound().compareTo(a.getRound()))
                    .limit(5)
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            log.info("Successfully fetched {} lottery numbers from Redis", results.size());
            return LotteryResponse.builder()
                    .success(true)
                    .data(results)
                    .lastUpdated(LocalDateTime.now().toString())
                    .build();

        } catch (Exception e) {
            log.error("Error fetching lottery numbers from Redis", e);
            return LotteryResponse.builder()
                    .success(false)
                    .data(getFallbackData())
                    .message("서버 오류로 인해 fallback 데이터를 사용합니다.")
                    .lastUpdated(LocalDateTime.now().toString())
                    .build();
        }
    }

    /**
     * 최신 당첨번호 조회 및 저장 (스케줄러에서 호출)
     */
    @Transactional
    public void fetchAndSaveLatestLotteryNumber() {
        log.info("Starting to fetch and save latest lottery numbers...");

        int estimatedCurrentRound = getCurrentEstimatedRound();

        // 최대 10회까지 역순으로 확인하여 새로운 회차 저장
        for (int i = 0; i < 10; i++) {
            int round = estimatedCurrentRound - i;

            // 이미 저장된 회차는 스킵
            if (lotteryNumberRepository.existsByRound(round)) {
                log.debug("Round {} already exists, skipping", round);
                continue;
            }

            // API에서 데이터 조회
            LotteryResponse.LotteryNumber lotteryData = fetchLotteryNumberFromApi(round);

            if (lotteryData != null) {
                // Redis에 저장
                LotteryNumber entity = LotteryNumber.builder()
                        .id("lottery:" + lotteryData.getRound())
                        .round(lotteryData.getRound())
                        .drawDate(lotteryData.getDate())
                        .number1(lotteryData.getNumbers().get(0))
                        .number2(lotteryData.getNumbers().get(1))
                        .number3(lotteryData.getNumbers().get(2))
                        .number4(lotteryData.getNumbers().get(3))
                        .number5(lotteryData.getNumbers().get(4))
                        .number6(lotteryData.getNumbers().get(5))
                        .bonusNumber(lotteryData.getBonus())
                        .createdAt(LocalDateTime.now())
                        .build();

                lotteryNumberRepository.save(entity);
                log.info("Saved lottery number for round {} in Redis", round);
            }
        }

        log.info("Completed fetching and saving lottery numbers");
    }

    /**
     * 특정 회차 로또 번호 API 조회
     */
    private LotteryResponse.LotteryNumber fetchLotteryNumberFromApi(int round) {
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
     * Entity -> DTO 변환
     */
    private LotteryResponse.LotteryNumber convertToDto(LotteryNumber entity) {
        return LotteryResponse.LotteryNumber.builder()
                .round(entity.getRound())
                .date(entity.getDrawDate())
                .numbers(Arrays.asList(
                    entity.getNumber1(),
                    entity.getNumber2(),
                    entity.getNumber3(),
                    entity.getNumber4(),
                    entity.getNumber5(),
                    entity.getNumber6()
                ))
                .bonus(entity.getBonusNumber())
                .build();
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

    /**
     * 이번 주 추천 번호 조회 (없으면 생성)
     */
    @Transactional
    public com.vive.auth.entity.WeeklyRecommendation getWeeklyRecommendations() {
        String weekKey = getCurrentWeekKey();
        String id = "weekly:" + weekKey;

        return weeklyRecommendationRepository.findById(id)
                .orElseGet(() -> generateWeeklyRecommendations(weekKey));
    }

    /**
     * 주차별 추천 번호 생성
     */
    @Transactional
    public com.vive.auth.entity.WeeklyRecommendation generateWeeklyRecommendations(String weekKey) {
        log.info("Generating weekly recommendations for week: {}", weekKey);

        // 역대 당첨번호 조합 Set 생성
        Set<String> winningNumbersSet = getAllWinningNumbersSet();

        // 5개의 유니크한 번호 생성
        List<com.vive.auth.entity.WeeklyRecommendation.RecommendationSet> recommendations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            com.vive.auth.entity.WeeklyRecommendation.RecommendationSet recommendationSet = generateUniqueNumberSet(winningNumbersSet);
            recommendations.add(recommendationSet);
        }

        // Redis에 저장
        com.vive.auth.entity.WeeklyRecommendation weeklyRecommendation = com.vive.auth.entity.WeeklyRecommendation.builder()
                .id("weekly:" + weekKey)
                .weekKey(weekKey)
                .generatedAt(LocalDateTime.now())
                .recommendations(recommendations)
                .build();

        weeklyRecommendationRepository.save(weeklyRecommendation);
        log.info("Successfully generated and saved {} recommendations for week {}", recommendations.size(), weekKey);

        return weeklyRecommendation;
    }

    /**
     * 역대 당첨번호 조합 Set 생성
     */
    private Set<String> getAllWinningNumbersSet() {
        List<LotteryNumber> allNumbers = lotteryNumberRepository.findAll();

        return allNumbers.stream()
                .map(this::numbersToKey)
                .collect(Collectors.toSet());
    }

    /**
     * 번호 조합을 문자열 키로 변환
     */
    private String numbersToKey(LotteryNumber lotteryNumber) {
        List<Integer> numbers = Arrays.asList(
            lotteryNumber.getNumber1(),
            lotteryNumber.getNumber2(),
            lotteryNumber.getNumber3(),
            lotteryNumber.getNumber4(),
            lotteryNumber.getNumber5(),
            lotteryNumber.getNumber6()
        );
        return numbers.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private String numbersToKey(List<Integer> numbers) {
        return numbers.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * 역대 당첨번호와 겹치지 않는 유니크한 번호 세트 생성
     */
    private com.vive.auth.entity.WeeklyRecommendation.RecommendationSet generateUniqueNumberSet(Set<String> winningNumbersSet) {
        Random random = new Random();
        int maxAttempts = 1000;
        int attempts = 0;

        while (attempts < maxAttempts) {
            // 6개의 메인 번호 생성
            List<Integer> numbers = new ArrayList<>();
            while (numbers.size() < 6) {
                int randomNum = random.nextInt(45) + 1;
                if (!numbers.contains(randomNum)) {
                    numbers.add(randomNum);
                }
            }
            Collections.sort(numbers);

            // 역대 당첨번호와 비교
            String key = numbersToKey(numbers);
            if (!winningNumbersSet.contains(key)) {
                // 보너스 번호 생성 (메인 번호와 중복되지 않게)
                int bonusNumber;
                do {
                    bonusNumber = random.nextInt(45) + 1;
                } while (numbers.contains(bonusNumber));

                return com.vive.auth.entity.WeeklyRecommendation.RecommendationSet.builder()
                        .numbers(numbers)
                        .bonusNumber(bonusNumber)
                        .build();
            }

            attempts++;
        }

        // 최대 시도 후에도 못 찾으면 일반 번호 반환
        log.warn("Could not find unique number set after {} attempts, returning random numbers", maxAttempts);
        List<Integer> numbers = new ArrayList<>();
        Random rand = new Random();
        while (numbers.size() < 6) {
            int randomNum = rand.nextInt(45) + 1;
            if (!numbers.contains(randomNum)) {
                numbers.add(randomNum);
            }
        }
        Collections.sort(numbers);

        int bonusNumber;
        do {
            bonusNumber = rand.nextInt(45) + 1;
        } while (numbers.contains(bonusNumber));

        return com.vive.auth.entity.WeeklyRecommendation.RecommendationSet.builder()
                .numbers(numbers)
                .bonusNumber(bonusNumber)
                .build();
    }

    /**
     * 현재 주차 키 반환 (예: "2024-W01")
     */
    private String getCurrentWeekKey() {
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int year = now.getYear();
        int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
        return String.format("%d-W%02d", year, weekNumber);
    }
}
