package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.GroupDto;
import com.example.project.dto.GroupFeedDto;
import com.example.project.dto.GroupFeedSectionResponse;
import com.example.project.dto.GroupMemberDto;
import com.example.project.dto.MyGroupDto;
import com.example.project.dto.request.GroupCreateRequest;
import com.example.project.entity.GroupPost;
import com.example.project.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    // 그룹 생성 (만든+가입한 그룹 총합 ≤ 3 제한은 Service에서 검증)
    @PostMapping
    public ResponseEntity<ApiResponse<GroupDto>> createGroup(@RequestBody GroupCreateRequest req,
                                                             @RequestAttribute Long userId) {
        GroupDto dto = groupService.createGroup(req, userId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // 그룹 수정 (그룹장만 가능)
    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDto>> updateGroup(@PathVariable Long groupId,
                                                             @RequestAttribute Long userId,
                                                             @RequestBody GroupCreateRequest req) {
        GroupDto dto = groupService.updateGroup(groupId, userId, req);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // 그룹 삭제 (그룹장만 가능)
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long groupId,
                                                         @RequestAttribute Long userId) {
        groupService.deleteGroup(groupId, userId);
        // ApiResponse 본문 없이 204 반환
        return ResponseEntity.noContent().build();
    }

    // 그룹 참여 요청 (총합 ≤ 3 제한은 Service에서 검증)
    @PostMapping("/{groupId}/join")
    public ResponseEntity<ApiResponse<Void>> requestToJoin(@PathVariable Long groupId,
                                                           @RequestAttribute Long userId) {
        groupService.requestToJoin(groupId, userId);
        return ResponseEntity.noContent().build();
    }
    
    /** ✅ 가입 요청 조회 (그룹장 전용) */
@GetMapping("/{groupId}/join-requests")
public ResponseEntity<ApiResponse<Page<GroupMemberDto>>> getJoinRequests(
        @PathVariable Long groupId,
        @RequestAttribute Long userId,          // 로그인 사용자(=그룹장) ID
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    Page<GroupMemberDto> result = groupService.getJoinRequests(groupId, userId, pageable);
    return ResponseEntity.ok(ApiResponse.success(result));
}

    // 그룹 가입 요청 승인 (그룹장 전용, 승인 대상자 총합 ≤ 3 검증)
    @PostMapping("/{groupId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptJoinRequest(@PathVariable Long groupId,
                                                               @RequestAttribute Long userId,
                                                               @RequestParam Long memberUserId) {
        groupService.acceptJoinRequest(groupId, userId, memberUserId);
        return ResponseEntity.noContent().build();
    }
    
        /** ✅ 가입 요청 거절 (그룹장 전용) */
    @PostMapping("/{groupId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectJoinRequest(@PathVariable Long groupId,
                                                               @RequestAttribute Long userId,
                                                               @RequestParam Long memberUserId) {
        groupService.rejectJoinRequest(groupId, userId, memberUserId);
        return ResponseEntity.noContent().build();
    }


    // 그룹장 위임 (신규 오너의 총합 ≤ 3 검증)
    @PutMapping("/{groupId}/delegate-owner")
    public ResponseEntity<ApiResponse<Void>> delegateOwner(@PathVariable Long groupId,
                                                           @RequestAttribute Long userId,
                                                           @RequestParam Long newOwnerId) {
        groupService.delegateOwner(groupId, userId, newOwnerId);
        return ResponseEntity.noContent().build();
    }

    // 그룹 탈퇴
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(@PathVariable Long groupId,
                                                        @RequestAttribute Long userId) {
        groupService.leaveGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    // 그룹 검색 (키워드 또는 카테고리)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<GroupDto>>> searchGroups(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        List<GroupDto> list = groupService.searchGroups(keyword, category);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    // 그룹 프로필 조회
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDto>> getGroupProfile(@PathVariable Long groupId) {
        GroupDto dto = groupService.getGroupProfile(groupId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // 그룹 멤버 조회 (ACCEPTED만)
    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<Page<GroupMemberDto>>> getGroupMembers(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "joinedAt"));
        Page<GroupMemberDto> result = groupService.getGroupMembers(groupId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 내 그룹 조회 (현재 로그인 사용자 기준)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<MyGroupDto>>> getMyGroups(@RequestAttribute Long userId) {
        List<MyGroupDto> list = groupService.getMyGroups(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    // ✅ 그룹 피드 조회: 섹션 응답(공지/추천도서/공동목표 pinned + 일반 최신 피드)
    @GetMapping("/{groupId}/feed")
public ResponseEntity<ApiResponse<GroupFeedSectionResponse>> getGroupFeed(
        @PathVariable Long groupId,
        @RequestParam(required = false) List<GroupPost.PostType> types,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestAttribute Long userId // ✔ 현재 로그인 사용자
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    var result = groupService.getGroupFeed(groupId, userId, types, pageable);
    return ResponseEntity.ok(ApiResponse.success(result));
}
}
