package com.vive.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vive.auth.dto.LottoHistoryRequest;
import com.vive.auth.dto.LottoHistoryResponse;
import com.vive.auth.entity.LottoHistory;
import com.vive.auth.entity.User;
import com.vive.auth.repository.LottoHistoryRepository;
import com.vive.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LottoService {

    private final LottoHistoryRepository lottoHistoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public LottoHistoryResponse saveLottoHistory(String email, LottoHistoryRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        try {
            String numbersJson = objectMapper.writeValueAsString(request.getNumbers());

            LottoHistory history = LottoHistory.builder()
                    .user(user)
                    .numbers(numbersJson)
                    .bonusNumber(request.getBonusNumber())
                    .build();

            LottoHistory savedHistory = lottoHistoryRepository.save(history);
            return LottoHistoryResponse.from(savedHistory);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to save lotto history", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<LottoHistoryResponse> getLottoHistories(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<LottoHistory> histories = lottoHistoryRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return histories.map(LottoHistoryResponse::from);
    }

    @Transactional(readOnly = true)
    public List<LottoHistoryResponse> getAllLottoHistories(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<LottoHistory> histories = lottoHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        return histories.stream()
                .map(LottoHistoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteLottoHistory(String email, Long historyId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        lottoHistoryRepository.deleteByUserAndId(user, historyId);
    }

    @Transactional
    public void deleteAllLottoHistories(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        lottoHistoryRepository.deleteByUser(user);
    }

    @Transactional(readOnly = true)
    public long countLottoHistories(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return lottoHistoryRepository.countByUser(user);
    }
}
