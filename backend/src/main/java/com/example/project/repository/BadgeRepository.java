package com.example.project.repository;

import com.example.project.entity.Badge;
import com.example.project.entity.User;
import com.example.project.enums.BadgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    boolean existsByUserAndBadgeType(User user, BadgeType badgeType);

    Optional<Badge> findByUserAndBadgeType(User user, BadgeType badgeType);

    List<Badge> findAllByUserId(Long userId);
    List<Badge> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
