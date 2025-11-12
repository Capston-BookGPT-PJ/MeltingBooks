package com.example.project.repository;

import com.example.project.entity.ExpEvent;
import com.example.project.entity.User;
import com.example.project.enums.ExpEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ExpEventRepository extends JpaRepository<ExpEvent, Long> {

    // 단순 중복 이벤트 확인
    Optional<ExpEvent> findByUserAndEventType(User user, ExpEventType eventType);

    // 기간 제한 이벤트 확인 (월간/연간)
    Optional<ExpEvent> findByUserAndEventTypeAndCreatedAtBetween(
            User user,
            ExpEventType eventType,
            LocalDateTime start,
            LocalDateTime end
    );
}
