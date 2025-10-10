package com.vive.auth.controller;

import com.vive.auth.dto.LotteryResponse;
import com.vive.auth.service.LotteryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Lottery", description = "로또 번호 조회 API")
@RestController
@RequestMapping("/api/lottery")
@RequiredArgsConstructor
public class LotteryController {

    private final LotteryService lotteryService;

    @Operation(summary = "최근 5회 로또 당첨번호 조회", description = "최근 5회차의 로또 당첨번호를 조회합니다. (캐싱됨)")
    @GetMapping("/recent")
    public ResponseEntity<LotteryResponse> getRecentLotteryNumbers() {
        LotteryResponse response = lotteryService.getRecentLotteryNumbers();
        return ResponseEntity.ok(response);
    }
}
