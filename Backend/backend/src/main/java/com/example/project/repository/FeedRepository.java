package com.example.project.repository;

import com.example.project.entity.Feed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedRepository extends JpaRepository<Feed, Long> {
    List<Feed> findByUser_Id(Long userId, Pageable pageable);
}
