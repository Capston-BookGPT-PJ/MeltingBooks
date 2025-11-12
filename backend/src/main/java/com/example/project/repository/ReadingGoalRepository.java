package com.example.project.repository;

import com.example.project.entity.ReadingGoal;
import com.example.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingGoalRepository extends JpaRepository<ReadingGoal, Long> {

    /** 사용자 + 특정 기간(오늘 포함 X: start < today, end > today) */
    List<ReadingGoal> findByUserIdAndStartDateBeforeAndEndDateAfter(Long userId, LocalDate start, LocalDate end);

    /** 사용자 + 특정 기간(오늘 포함 O: start <= today, end >= today) */
    List<ReadingGoal> findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long userId, LocalDate start, LocalDate end);

    /** 사용자 전체 목표 조회 */
    List<ReadingGoal> findByUserId(Long userId);

    /** 유저 삭제 시 목표 일괄 정리 */
    void deleteByUser(User user);

    /**
     * 사용자 + 유형 + 연도 + (월)로 목표 단건 조회
     * - YEARLY의 경우 month는 NULL
     * - MONTHLY의 경우 month는 1~12
     */
    Optional<ReadingGoal> findByUserIdAndGoalTypeAndYearAndMonth(
            Long userId,
            ReadingGoal.GoalType goalType,
            int year,
            Integer month
    );

    /** goalId + userId 로 소유권 검증용 조회 */
    Optional<ReadingGoal> findByIdAndUserId(Long id, Long userId);

    /** goalId + userId 로 바로 삭제 (선호 시 사용) */
    void deleteByIdAndUserId(Long id, Long userId);

    /** 해당 기간에 동일 타입 목표 중복 방지 체크 */
    boolean existsByUserIdAndGoalTypeAndYearAndMonth(Long userId, ReadingGoal.GoalType goalType, int year, Integer month);

    /**
     * ✅ 활성 목표 조회:
     *  - 기간형: today가 start~end 사이 (포함)
     *  - 월간형: goalType=MONTHLY AND year=:year AND month=:month
     *  - 연간형: goalType=YEARLY  AND year=:year
     *
     *  start/end가 비어있는 월간/연간 목표도 활성으로 잡아주기 위함.
     */
    @Query("""
        select g from ReadingGoal g
        where g.user.id = :userId
          and (
                (g.startDate is not null and g.endDate is not null and :today between g.startDate and g.endDate)
             or (g.goalType = com.example.project.entity.ReadingGoal$GoalType.MONTHLY and g.year = :year and g.month = :month)
             or (g.goalType = com.example.project.entity.ReadingGoal$GoalType.YEARLY  and g.year = :year)
          )
    """)
    List<ReadingGoal> findActiveGoals(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("year") int year,
            @Param("month") Integer month
    );
}
