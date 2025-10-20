package com.vive.auth.repository;

import com.vive.auth.entity.LottoHistory;
import com.vive.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LottoHistoryRepository extends JpaRepository<LottoHistory, Long> {

    Page<LottoHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<LottoHistory> findByUserOrderByCreatedAtDesc(User user);

    void deleteByUserAndId(User user, Long id);

    void deleteByUser(User user);

    long countByUser(User user);

    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}
