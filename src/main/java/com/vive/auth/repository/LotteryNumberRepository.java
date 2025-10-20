package com.vive.auth.repository;

import com.vive.auth.entity.LotteryNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LotteryNumberRepository extends JpaRepository<LotteryNumber, Long> {

    // 특정 회차로 조회
    Optional<LotteryNumber> findByRound(Integer round);

    // 특정 회차 존재 여부 확인
    boolean existsByRound(Integer round);
}
