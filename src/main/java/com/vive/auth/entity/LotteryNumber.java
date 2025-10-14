package com.vive.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "lottery_numbers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class LotteryNumber {

    @Id
    @Column(unique = true, nullable = false)
    private Integer round;

    @Column(nullable = false)
    private String drawDate;

    @Column(nullable = false)
    private Integer number1;

    @Column(nullable = false)
    private Integer number2;

    @Column(nullable = false)
    private Integer number3;

    @Column(nullable = false)
    private Integer number4;

    @Column(nullable = false)
    private Integer number5;

    @Column(nullable = false)
    private Integer number6;

    @Column(nullable = false)
    private Integer bonusNumber;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
