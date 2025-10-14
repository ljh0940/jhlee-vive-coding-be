package com.vive.auth.repository;

import com.vive.auth.entity.LotteryNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotteryNumberRepository extends JpaRepository<LotteryNumber, Integer> {

    // 최근 5개 조회 (round 내림차순)
    List<LotteryNumber> findTop5ByOrderByRoundDesc();

    // 특정 회차 존재 여부 확인
    boolean existsByRound(Integer round);
}
