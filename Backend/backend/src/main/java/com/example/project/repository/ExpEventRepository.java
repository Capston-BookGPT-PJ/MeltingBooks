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

    // �ܼ� �ߺ� �̺�Ʈ Ȯ��
    Optional<ExpEvent> findByUserAndEventType(User user, ExpEventType eventType);

    // �Ⱓ ���� �̺�Ʈ Ȯ�� (����/����)
    Optional<ExpEvent> findByUserAndEventTypeAndCreatedAtBetween(
            User user,
            ExpEventType eventType,
            LocalDateTime start,
            LocalDateTime end
    );
}
