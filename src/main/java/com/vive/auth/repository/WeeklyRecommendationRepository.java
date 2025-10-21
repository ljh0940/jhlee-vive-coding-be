package com.vive.auth.repository;

import com.vive.auth.entity.WeeklyRecommendation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeeklyRecommendationRepository extends CrudRepository<WeeklyRecommendation, String> {

    Optional<WeeklyRecommendation> findByWeekKey(String weekKey);
}
