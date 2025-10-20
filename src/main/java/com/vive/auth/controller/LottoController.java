package com.vive.auth.controller;

import com.vive.auth.dto.LottoHistoryRequest;
import com.vive.auth.dto.LottoHistoryResponse;
import com.vive.auth.service.LottoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lotto")
@RequiredArgsConstructor
public class LottoController {

    private final LottoService lottoService;

    @PostMapping("/history")
    public ResponseEntity<LottoHistoryResponse> saveLottoHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody LottoHistoryRequest request) {
        LottoHistoryResponse response = lottoService.saveLottoHistory(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<LottoHistoryResponse>> getLottoHistories(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<LottoHistoryResponse> histories = lottoService.getLottoHistories(
                userDetails.getUsername(), page, size);
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/history/all")
    public ResponseEntity<List<LottoHistoryResponse>> getAllLottoHistories(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<LottoHistoryResponse> histories = lottoService.getAllLottoHistories(userDetails.getUsername());
        return ResponseEntity.ok(histories);
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> deleteLottoHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        lottoService.deleteLottoHistory(userDetails.getUsername(), id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> deleteAllLottoHistories(
            @AuthenticationPrincipal UserDetails userDetails) {
        lottoService.deleteAllLottoHistories(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history/count")
    public ResponseEntity<Long> countLottoHistories(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = lottoService.countLottoHistories(userDetails.getUsername());
        return ResponseEntity.ok(count);
    }
}
