package com.example.project.repository;

import com.example.project.entity.ReadingLog;
import com.example.project.entity.Book;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;

@Repository
public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long> {

    // (가능하면 사용하지 않기) 엔티티 파라미터 기반
    List<ReadingLog> findByUser(com.example.project.entity.User user);
    void deleteByUser(com.example.project.entity.User user);

    // ✅ 안전한 id 기반 조회 (최신순)
    List<ReadingLog> findByUser_IdOrderByReadAtDesc(Long userId, Pageable pageable);

    /* =========================
       전체 합계 (유저 단위)
       ========================= */
    @Query("""
        select coalesce(sum(r.pagesRead), 0)
        from ReadingLog r
        where r.user.id = :userId
    """)
    long sumPagesByUserId(@Param("userId") Long userId);

    @Query("""
        select coalesce(sum(r.minutesRead), 0)
        from ReadingLog r
        where r.user.id = :userId
    """)
    long sumMinutesByUserId(@Param("userId") Long userId);

    /* =========================
       개인 완독 수: finished=true 기준 (distinct book)
       ========================= */
    @Query("""
        select coalesce(count(distinct r.book.id), 0)
        from ReadingLog r
        where r.user.id = :userId
          and r.finished = true
    """)
    long countCompletedBooksByUserId(@Param("userId") Long userId);

    /* =========================
       readAt 기준 최초/최신
       ========================= */
    @Query("select min(r.readAt) from ReadingLog r where r.user.id = :userId")
    LocalDateTime findFirstLogDateByUserId(@Param("userId") Long userId);

    @Query("select max(r.readAt) from ReadingLog r where r.user.id = :userId")
    LocalDateTime findLastLogDateByUserId(@Param("userId") Long userId);

    /* =========================
       기간 합계: [start, end) (끝 미포함)
       ========================= */
    @Query("""
        select coalesce(sum(r.pagesRead), 0)
        from ReadingLog r
        where r.user.id = :userId
          and r.readAt >= :start
          and r.readAt <  :end
    """)
    long sumPagesByUserIdAndPeriod(@Param("userId") Long userId,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    @Query("""
        select coalesce(sum(r.minutesRead), 0)
        from ReadingLog r
        where r.user.id = :userId
          and r.readAt >= :start
          and r.readAt <  :end
    """)
    long sumMinutesByUserIdAndPeriod(@Param("userId") Long userId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    /* =========================
       그룹(멤버 합산) 집계: [start, end)
       ========================= */
    @Query("""
        select coalesce(sum(r.minutesRead), 0)
        from ReadingLog r
        where r.user.id in :userIds
          and r.readAt >= :start
          and r.readAt <  :end
    """)
    long sumMinutesByUsersAndPeriod(@Param("userIds") Collection<Long> userIds,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    @Query("""
        select coalesce(count(distinct r.book.id), 0)
        from ReadingLog r
        where r.user.id in :userIds
          and r.finished = true
          and r.finishedAt >= :start
          and r.finishedAt <  :end
    """)
    long countCompletedBooksByUsersAndPeriod(@Param("userIds") Collection<Long> userIds,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    /* =========================
       기간 조회 (정렬 포함): [start, end)
       엔티티 PK가 'id'가 아닐 경우 r.id를 실제 PK 필드명으로 변경
       ========================= */
    @Query("""
        select r
        from ReadingLog r
        where r.user.id = :userId
          and r.readAt >= :start
          and r.readAt <  :end
        order by r.readAt desc, r.id desc
    """)
    List<ReadingLog> findLogsInRange(@Param("userId") Long userId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    /* =========================
       목표 상세의 books용: 기간 내 완독 도서 (finishedAt 기준 [start, end))
       ========================= */
    @Query("""
        select distinct rl.book
        from ReadingLog rl
        where rl.user.id = :userId
          and rl.finished = true
          and rl.finishedAt >= :start
          and rl.finishedAt <  :end
    """)
    List<Book> findFinishedBooksByUserAndPeriod(@Param("userId") Long userId,
                                                @Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);
}
