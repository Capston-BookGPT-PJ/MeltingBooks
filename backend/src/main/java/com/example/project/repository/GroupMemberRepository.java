package com.example.project.repository;

import com.example.project.entity.Group;
import com.example.project.entity.GroupMember;
import com.example.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    int countByUserIdAndJoinStatus(Long userId, GroupMember.JoinStatus joinStatus);

    int countByGroupAndJoinStatus(Group group, GroupMember.JoinStatus joinStatus);
    int countByGroupIdAndJoinStatus(Long groupId, GroupMember.JoinStatus joinStatus);

    Optional<GroupMember> findByGroupAndUser(Group group, User user);

    Page<GroupMember> findByGroupIdAndJoinStatus(Long groupId, GroupMember.JoinStatus joinStatus, Pageable pageable);

    List<GroupMember> findByUserIdAndJoinStatus(Long userId, GroupMember.JoinStatus joinStatus);

    List<GroupMember> findTop3ByUserIdAndJoinStatusOrderByJoinedAtDesc(Long userId, GroupMember.JoinStatus joinStatus);
}
