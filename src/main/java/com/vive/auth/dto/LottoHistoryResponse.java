package com.vive.auth.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vive.auth.entity.LottoHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LottoHistoryResponse {
    private Long id;
    private List<Integer> numbers;
    private Integer bonusNumber;
    private LocalDateTime createdAt;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static LottoHistoryResponse from(LottoHistory history) {
        try {
            List<Integer> numbers = objectMapper.readValue(
                    history.getNumbers(),
                    new TypeReference<List<Integer>>() {}
            );

            return LottoHistoryResponse.builder()
                    .id(history.getId())
                    .numbers(numbers)
                    .bonusNumber(history.getBonusNumber())
                    .createdAt(history.getCreatedAt())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse lotto numbers", e);
        }
    }
}
