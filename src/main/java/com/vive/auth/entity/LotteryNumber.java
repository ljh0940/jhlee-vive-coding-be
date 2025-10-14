package com.vive.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@RedisHash("LotteryNumber")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryNumber {

    @Id
    private String id; // "lottery:{round}"

    @Indexed
    private Integer round;

    private String drawDate;

    private Integer number1;

    private Integer number2;

    private Integer number3;

    private Integer number4;

    private Integer number5;

    private Integer number6;

    private Integer bonusNumber;

    private LocalDateTime createdAt;
}
