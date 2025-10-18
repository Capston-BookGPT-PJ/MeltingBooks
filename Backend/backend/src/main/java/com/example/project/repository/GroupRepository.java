package com.example.project.repository;

import com.example.project.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    // 이름 키워드 검색 (대소문자 무시)
    List<Group> findByNameContainingIgnoreCase(String name);

    // 카테고리 정확 일치
    List<Group> findByCategory(String category);

    // 특정 유저가 소유한 그룹 개수
    int countByOwnerId(Long ownerId);
}
