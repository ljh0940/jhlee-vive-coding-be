package com.vive.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LottoHistoryRequest {
    private List<Integer> numbers;
    private Integer bonusNumber;
}
