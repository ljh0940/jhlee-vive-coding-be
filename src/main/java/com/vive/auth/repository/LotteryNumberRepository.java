package com.vive.auth.repository;

import com.vive.auth.entity.LotteryNumber;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LotteryNumberRepository extends CrudRepository<LotteryNumber, String> {

    // 특정 회차로 조회
    Optional<LotteryNumber> findByRound(Integer round);

    // 특정 회차 존재 여부 확인
    boolean existsByRound(Integer round);

    // 모든 데이터 조회 (round로 정렬은 서비스에서 처리)
    List<LotteryNumber> findAll();
}
