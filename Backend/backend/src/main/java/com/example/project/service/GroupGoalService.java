package com.example.project.service;

import com.example.project.dto.GroupGoalDto;
import com.example.project.entity.Group;
import com.example.project.entity.GroupGoal;
import com.example.project.entity.GroupMember;
import com.example.project.entity.User;
import com.example.project.repository.GroupGoalRepository;
import com.example.project.repository.GroupMemberRepository;
import com.example.project.repository.GroupRepository;
import com.example.project.repository.ReadingLogRepository;
import com.example.project.repository.ReviewRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupGoalService {

    private final GroupGoalRepository groupGoalRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ReadingLogRepository readingLogRepository;
    private final ReviewRepository reviewRepository;

    /* -----------------------------
       생성 / 수정 / 삭제
       ----------------------------- */

    @Transactional
    public GroupGoalDto create(Long groupId, Long creatorUserId, GroupGoalDto dto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 그룹장만 생성 허용 (정책에 맞춰 유지)
        if (!group.getOwner().getId().equals(creatorUserId)) {
            throw new IllegalStateException("그룹장만 목표를 생성할 수 있습니다.");
        }

        GroupGoal goal = GroupGoal.builder()
                .group(group)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .targetBooks(n(dto.getTargetBooks()))
                .targetReviews(n(dto.getTargetReviews()))
                .targetMinutes(n(dto.getTargetMinutes()))
                .completedBooks(0)
                .completedReviews(0)
                .completedMinutes(0)
                .createdBy(creator)
                .status(GroupGoal.Status.ACTIVE)
                .build();

        goal.updateStatusByProgress(LocalDate.now());
        return GroupGoalDto.fromEntity(groupGoalRepository.save(goal));
    }

    @Transactional
    public GroupGoalDto update(Long goalId, Long userId, GroupGoalDto dto) {
        GroupGoal goal = groupGoalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));

        if (!goal.getGroup().getOwner().getId().equals(userId)) {
            throw new IllegalStateException("그룹장만 목표를 수정할 수 있습니다.");
        }

        goal.setTitle(dto.getTitle());
        goal.setDescription(dto.getDescription());
        goal.setStartDate(dto.getStartDate());
        goal.setEndDate(dto.getEndDate());
        goal.setTargetBooks(n(dto.getTargetBooks()));
        goal.setTargetReviews(n(dto.getTargetReviews()));
        goal.setTargetMinutes(n(dto.getTargetMinutes()));

        goal.updateStatusByProgress(LocalDate.now());
        return GroupGoalDto.fromEntity(groupGoalRepository.save(goal));
    }

    @Transactional
    public void delete(Long goalId, Long userId) {
        GroupGoal goal = groupGoalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));
        if (!goal.getGroup().getOwner().getId().equals(userId)) {
            throw new IllegalStateException("그룹장만 목표를 삭제할 수 있습니다.");
        }
        groupGoalRepository.delete(goal);
    }

    /* -----------------------------
       내부 유틸: 멤버/기간
       ----------------------------- */

    private static class Range {
        final LocalDateTime start;        // inclusive
        final LocalDateTime endExclusive; // exclusive
        Range(LocalDateTime s, LocalDateTime eEx) { this.start = s; this.endExclusive = eEx; }
    }

    private static Range rangeOf(GroupGoal g) {
        return new Range(
                g.getStartDate().atStartOfDay(),
                g.getEndDate().plusDays(1).atStartOfDay() // [start, end)
        );
    }

    private List<Long> acceptedMemberIds(Long groupId) {
        return groupMemberRepository
                .findByGroupIdAndJoinStatus(groupId, GroupMember.JoinStatus.ACCEPTED, Pageable.unpaged())
                .getContent()
                .stream()
                .map(m -> m.getUser().getId())
                .collect(Collectors.toList());
    }

    /* -----------------------------
       조회 — 원천 데이터 즉석 집계 반영
       ----------------------------- */

    @Transactional(readOnly = true)
    public List<GroupGoalDto> findByGroup(Long groupId) {
        var goals = groupGoalRepository.findByGroup_Id(groupId);
        List<Long> memberUserIds = acceptedMemberIds(groupId);

        return goals.stream().map(g -> {
            GroupGoalDto dto = GroupGoalDto.fromEntity(g);

            if (!memberUserIds.isEmpty()) {
                Range r = rangeOf(g);
                long minutes = readingLogRepository
                        .sumMinutesByUsersAndPeriod(memberUserIds, r.start, r.endExclusive);
                long books = readingLogRepository
                        .countCompletedBooksByUsersAndPeriod(memberUserIds, r.start, r.endExclusive);
                long reviews = reviewRepository
                        .countByUsersAndCreatedAtRange(memberUserIds, r.start, r.endExclusive);

                dto.setCompletedMinutes(Math.toIntExact(minutes));
                dto.setCompletedBooks(Math.toIntExact(books));
                dto.setCompletedReviews(Math.toIntExact(reviews)); // ✅ 실제 리뷰 수 반영
            } else {
                dto.setCompletedMinutes(0);
                dto.setCompletedBooks(0);
                dto.setCompletedReviews(0);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupGoalDto get(Long goalId) {
        GroupGoal g = groupGoalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));

        GroupGoalDto dto = GroupGoalDto.fromEntity(g);

        List<Long> memberUserIds = acceptedMemberIds(g.getGroup().getId());
        if (!memberUserIds.isEmpty()) {
            Range r = rangeOf(g);
            long minutes = readingLogRepository
                    .sumMinutesByUsersAndPeriod(memberUserIds, r.start, r.endExclusive);
            long books = readingLogRepository
                    .countCompletedBooksByUsersAndPeriod(memberUserIds, r.start, r.endExclusive);
            long reviews = reviewRepository
                    .countByUsersAndCreatedAtRange(memberUserIds, r.start, r.endExclusive);

            dto.setCompletedMinutes(Math.toIntExact(minutes));
            dto.setCompletedBooks(Math.toIntExact(books));
            dto.setCompletedReviews(Math.toIntExact(reviews)); // ✅ 실제 리뷰 수 반영
        } else {
            dto.setCompletedMinutes(0);
            dto.setCompletedBooks(0);
            dto.setCompletedReviews(0);
        }

        return dto;
    }

    /* -----------------------------
       진행 정합성 재계산(정답표)
       - 리뷰: 개인 Review만 집계 (그룹 피드 제외)
       - 시간/권수: 개인 ReadingLog 합산
       - 경계: [start, end) (끝 미포함)
       ----------------------------- */
    @Transactional
    public GroupGoalDto recompute(Long goalId) {
        GroupGoal goal = groupGoalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));

        List<Long> memberUserIds = acceptedMemberIds(goal.getGroup().getId());

        Range r = rangeOf(goal);

        int minutes = memberUserIds.isEmpty() ? 0
                : Math.toIntExact(readingLogRepository
                .sumMinutesByUsersAndPeriod(memberUserIds, r.start, r.endExclusive));

        int books = memberUserIds.isEmpty() ? 0
                : Math.toIntExact(readingLogRepository
                .countCompletedBooksByUsersAndPeriod(memberUserIds, r.start, r.endExclusive));

        int reviews = memberUserIds.isEmpty() ? 0
                : Math.toIntExact(reviewRepository
                .countByUsersAndCreatedAtRange(memberUserIds, r.start, r.endExclusive));

        goal.setCompletedMinutes(minutes);
        goal.setCompletedBooks(books);
        goal.setCompletedReviews(reviews);

        goal.updateStatusByProgress(LocalDate.now());
        return GroupGoalDto.fromEntity(groupGoalRepository.save(goal));
    }

    /* -----------------------------
       이벤트 훅
       - 개인 리뷰 생성/독서기록 생성 시 그룹 목표에 실시간 반영
       - 그룹 피드 글은 제외(정책)
       - ⚠️ 실시간 증분은 편의용, 정합성은 recompute가 진실원본
       ----------------------------- */

    /** 개인 리뷰(Review) 생성 시 호출 → 감상문 +1 */
    @Transactional
    public void onPersonalReviewCreated(Long authorUserId, LocalDateTime createdAt) {
        LocalDate date = (createdAt != null ? createdAt.toLocalDate() : LocalDate.now());

        List<Long> groupIds = groupMemberRepository
                .findByUserIdAndJoinStatus(authorUserId, GroupMember.JoinStatus.ACCEPTED)
                .stream()
                .map(m -> m.getGroup().getId())
                .collect(Collectors.toList());

        if (groupIds.isEmpty()) return;

        for (Long gid : groupIds) {
            List<GroupGoal> goals = groupGoalRepository.findActiveByGroupOnDate(gid, date);
            for (GroupGoal g : goals) {
                g.setCompletedReviews(g.getCompletedReviews() + 1);
                g.updateStatusByProgress(LocalDate.now());
            }
        }
        // @Transactional → Dirty Checking으로 저장됨
    }

    /** 개인 독서기록 생성 시 호출 → 시간 누적(minutes) */
    @Transactional
    public void onReadingLogCreated(Long userId, LocalDateTime readAt, int minutes) {
        if (minutes <= 0 || readAt == null) return;

        List<Long> groupIds = groupMemberRepository
                .findByUserIdAndJoinStatus(userId, GroupMember.JoinStatus.ACCEPTED)
                .stream()
                .map(m -> m.getGroup().getId())
                .collect(Collectors.toList());

        if (groupIds.isEmpty()) return;

        LocalDate date = readAt.toLocalDate();
        for (Long gid : groupIds) {
            List<GroupGoal> goals = groupGoalRepository.findActiveByGroupOnDate(gid, date);
            for (GroupGoal g : goals) {
                g.setCompletedMinutes(g.getCompletedMinutes() + minutes);
                g.updateStatusByProgress(LocalDate.now());
            }
        }
    }

    /* ----------------------------- */

    private static int n(Integer v) {
        return v == null ? 0 : v;
    }
}
