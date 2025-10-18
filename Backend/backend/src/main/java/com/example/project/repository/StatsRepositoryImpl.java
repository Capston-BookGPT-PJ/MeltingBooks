package com.example.project.repository;

import com.example.project.entity.GroupPost;
import com.example.project.entity.ReadingGoal.GoalType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StatsRepositoryImpl implements StatsRepository {

    private final GroupCommentRepository groupCommentRepository;
    private final GroupLikeRepository groupLikeRepository;
    private final GroupPostRepository groupPostRepository;

    @PersistenceContext
    private EntityManager em;

    /* ============================== *
     *   댓글(일반 리뷰) 관련 집계
     * ============================== */

    @Override
    public long countCommentsOnOthersReviews(Long userId) {
        return em.createQuery("""
                select count(c)
                from Comment c
                join c.review r
                where c.user.id = :userId
                  and r.user.id <> :userId
                """, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    @Override
    public long countMonthlyCommentsOnOthersReviews(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to   = ym.plusMonths(1).atDay(1).atStartOfDay();

        return em.createQuery("""
                select count(c)
                from Comment c
                join c.review r
                where c.user.id = :userId
                  and r.user.id <> :userId
                  and c.createdAt >= :from
                  and c.createdAt <  :to
                """, Long.class)
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
    }

    /* ============================== *
     *   그룹(그룹 리뷰/댓글/좋아요) 집계
     * ============================== */

    @Override
    public long countGroupCommentsOnOthersReviews(Long userId) {
        // 내가 단 댓글 + 대상 포스트가 REVIEW + 그 포스트의 작성자가 내가 아님
        return groupCommentRepository.countByUser_IdAndPost_PostTypeAndPost_User_IdNot(
                userId, GroupPost.PostType.REVIEW, userId
        );
    }

    @Override
    public long countLikesReceivedForMyGroupReviews(Long userId) {
        // 내가 쓴 그룹 REVIEW 포스트들이 받은 좋아요 합
        return groupLikeRepository.countByPost_User_IdAndPost_PostType(
                userId, GroupPost.PostType.REVIEW
        );
    }

    @Override
    public long countGroupReviews(Long userId) {
        // 내가 쓴 그룹 REVIEW 포스트 개수
        return groupPostRepository.countByUser_IdAndPostType(userId, GroupPost.PostType.REVIEW);
    }

    @Override
    public long countAchievedGroupGoals(Long userId) {
        // 아직 엔티티가 없다면 0L 반환(향후 실제 스키마 생기면 구현)
        return 0L;
    }

    /* ============================== *
     *   완독(ReadingLog) 관련 집계
     * ============================== */

    @Override
    public long countFinishedBooks(Long userId, LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end   = to.plusDays(1).atStartOfDay();
        return em.createQuery("""
                select count(distinct rl.book.id)
                from ReadingLog rl
                where rl.user.id = :userId
                  and rl.finished = true
                  and rl.finishedAt >= :start
                  and rl.finishedAt <  :end
                """, Long.class)
                .setParameter("userId", userId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    @Override
    public long countTotalFinishedBooks(Long userId) {
        return em.createQuery("""
                select count(distinct rl.book.id)
                from ReadingLog rl
                where rl.user.id = :userId
                  and rl.finished = true
                """, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    @Override
    public long countFinishedBooksByGenre(Long userId, String genre) {
        return em.createQuery("""
                select count(distinct rl.book.id)
                from ReadingLog rl
                where rl.user.id = :userId
                  and rl.finished = true
                  and rl.book.categoryName = :genre
                """, Long.class)
                .setParameter("userId", userId)
                .setParameter("genre", genre)
                .getSingleResult();
    }

    /** 서로 다른 장르 중, 각 장르에서 최소 minCount권 이상 완독한 장르의 개수 */
    @Override
    public long countGenresWithMinFinished(Long userId, int minCount) {
        List<?> rows = em.createQuery("""
                select rl.book.categoryName, count(distinct rl.book.id)
                from ReadingLog rl
                where rl.user.id = :userId
                  and rl.finished = true
                group by rl.book.categoryName
                having count(distinct rl.book.id) >= :minCount
                """)
                .setParameter("userId", userId)
                .setParameter("minCount", (long) minCount)
                .getResultList();
        return rows.size();
    }

    /* ============================== *
     *   목표(ReadingGoal) / 리뷰(Review) 집계
     * ============================== */

    @Override
    public long countAchievedMonthlyGoals(Long userId) {
        // 월간 목표: 책/시간/리뷰 중 하나라도 목표치 이상이면 달성으로 간주
        return em.createQuery("""
                select count(g)
                from ReadingGoal g
                where g.user.id = :userId
                  and g.goalType = :monthly
                  and (
                        (coalesce(g.targetBooks,0)   > 0 and g.completedBooks   >= g.targetBooks)
                     or (coalesce(g.targetMinutes,0) > 0 and g.completedMinutes >= g.targetMinutes)
                     or (coalesce(g.targetReviews,0) > 0 and g.completedReviews >= g.targetReviews)
                  )
                """, Long.class)
                .setParameter("userId", userId)
                .setParameter("monthly", GoalType.MONTHLY)
                .getSingleResult();
    }

    @Override
    public boolean isMonthlyGoalAchieved(Long userId, YearMonth ym) {
        Long cnt = em.createQuery("""
                select count(g)
                from ReadingGoal g
                where g.user.id = :userId
                  and g.goalType = :monthly
                  and g.year = :y
                  and g.month = :m
                  and (
                        (coalesce(g.targetBooks,0)   > 0 and g.completedBooks   >= g.targetBooks)
                     or (coalesce(g.targetMinutes,0) > 0 and g.completedMinutes >= g.targetMinutes)
                     or (coalesce(g.targetReviews,0) > 0 and g.completedReviews >= g.targetReviews)
                  )
                """, Long.class)
                .setParameter("userId", userId)
                .setParameter("monthly", GoalType.MONTHLY)
                .setParameter("y", ym.getYear())
                .setParameter("m", ym.getMonthValue())
                .getSingleResult();
        return cnt != null && cnt > 0;
    }

    @Override
    public Optional<Integer> getYearlyTarget(Long userId, int year) {
        List<Integer> targets = em.createQuery("""
                select g.targetBooks
                from ReadingGoal g
                where g.user.id = :userId
                  and g.goalType = :yearly
                  and g.year = :y
                order by g.id desc
                """, Integer.class)
                .setParameter("userId", userId)
                .setParameter("yearly", GoalType.YEARLY)
                .setParameter("y", year)
                .setMaxResults(1)
                .getResultList();
        return targets.isEmpty() ? Optional.empty() : Optional.ofNullable(targets.get(0));
    }

    @Override
    public long getYearlyProgress(Long userId, int year) {
        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end   = LocalDate.of(year + 1, 1, 1).atStartOfDay();
        return em.createQuery("""
                select count(distinct rl.book.id)
                from ReadingLog rl
                where rl.user.id = :userId
                  and rl.finished = true
                  and rl.finishedAt >= :start
                  and rl.finishedAt <  :end
                """, Long.class)
                .setParameter("userId", userId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    @Override
    public long countReviews(Long userId) {
        return em.createQuery("""
                select count(r)
                from Review r
                where r.user.id = :userId
                """, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    @Override
    public long countMonthlyReviews(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to   = ym.plusMonths(1).atDay(1).atStartOfDay();
        return em.createQuery("""
                select count(r)
                from Review r
                where r.user.id = :userId
                  and r.createdAt >= :from
                  and r.createdAt <  :to
                """, Long.class)
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
    }
}
