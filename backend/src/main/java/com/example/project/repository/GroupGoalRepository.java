package com.example.project.repository;

import com.example.project.entity.GroupGoal;
import com.example.project.entity.GroupGoal.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GroupGoalRepository extends JpaRepository<GroupGoal, Long> {

    List<GroupGoal> findByGroup_Id(Long groupId);

    @Query("""
        SELECT g FROM GroupGoal g
        WHERE g.group.id = :groupId
          AND g.startDate <= :date
          AND g.endDate >= :date
          AND g.status = :status
    """)
    List<GroupGoal> findByGroupOnDateWithStatus(@Param("groupId") Long groupId,
                                                @Param("date") LocalDate date,
                                                @Param("status") Status status);

    @Query("""
        SELECT g FROM GroupGoal g
        WHERE g.group.id IN :groupIds
          AND g.startDate <= :date
          AND g.endDate >= :date
          AND g.status = :status
    """)
    List<GroupGoal> findByGroupsOnDateWithStatus(@Param("groupIds") List<Long> groupIds,
                                                 @Param("date") LocalDate date,
                                                 @Param("status") Status status);

    // 편의 메서드: ACTIVE 고정
    default List<GroupGoal> findActiveByGroupOnDate(Long groupId, LocalDate date) {
        return findByGroupOnDateWithStatus(groupId, date, Status.ACTIVE);
    }

    // 빈 리스트 IN() 방지 포함
    default List<GroupGoal> findActiveByGroupsOnDate(List<Long> groupIds, LocalDate date) {
        if (groupIds == null || groupIds.isEmpty()) return List.of();
        return findByGroupsOnDateWithStatus(groupIds, date, Status.ACTIVE);
    }
}
