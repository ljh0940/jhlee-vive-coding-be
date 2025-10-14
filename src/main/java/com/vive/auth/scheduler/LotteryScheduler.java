package com.vive.auth.scheduler;

import com.vive.auth.service.LotteryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LotteryScheduler {

    private final LotteryService lotteryService;

    /**
     * 매주 토요일 오후 9시에 당첨번호 조회 및 저장
     * cron: "초 분 시 일 월 요일"
     * 0 0 21 * * SAT = 매주 토요일 21시 0분 0초
     */
    @Scheduled(cron = "0 0 21 * * SAT", zone = "Asia/Seoul")
    public void fetchAndSaveWeeklyLotteryNumbers() {
        log.info("Starting scheduled lottery numbers fetch...");
        try {
            lotteryService.fetchAndSaveLatestLotteryNumber();
            log.info("Successfully completed scheduled lottery numbers fetch");
        } catch (Exception e) {
            log.error("Failed to fetch and save lottery numbers", e);
        }
    }

    /**
     * 애플리케이션 시작 시 최신 데이터 확인 및 업데이트
     * 서버 시작 후 10초 뒤에 한 번 실행
     */
    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    public void initializeOnStartup() {
        log.info("Initializing lottery numbers on startup...");
        try {
            lotteryService.fetchAndSaveLatestLotteryNumber();
            log.info("Successfully initialized lottery numbers");
        } catch (Exception e) {
            log.error("Failed to initialize lottery numbers", e);
        }
    }
}
