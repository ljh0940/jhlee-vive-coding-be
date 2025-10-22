package com.vive.auth.repository;

import com.vive.auth.entity.WeeklyRecommendation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyRecommendationRepository extends CrudRepository<WeeklyRecommendation, String> {
}
