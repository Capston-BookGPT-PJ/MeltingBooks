package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.GroupCommentCreateRequest;
import com.example.project.dto.GroupCommentDto;
import com.example.project.dto.GroupCommentUpdateRequest;
import com.example.project.service.GroupCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups/{groupId}/posts/{postId}/comments")
@RequiredArgsConstructor
public class GroupCommentController {

    private final GroupCommentService groupCommentService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupCommentDto>> create(@PathVariable Long groupId,
                                                               @PathVariable Long postId,
                                                               @RequestAttribute Long userId,
                                                               @RequestBody GroupCommentCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                groupCommentService.add(groupId, postId, userId, req)
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GroupCommentDto>>> list(@PathVariable Long groupId,
                                                                   @PathVariable Long postId,
                                                                   Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                groupCommentService.list(groupId, postId, pageable)
        ));
    }

    @RequestMapping(value = "/{commentId}", method = { RequestMethod.PUT, RequestMethod.PATCH })
    public ResponseEntity<ApiResponse<GroupCommentDto>> update(@PathVariable Long groupId,
                                                               @PathVariable Long postId,
                                                               @PathVariable Long commentId,
                                                               @RequestAttribute Long userId,
                                                               @RequestBody GroupCommentUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                groupCommentService.update(groupId, postId, commentId, userId, req)
        ));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long groupId,
                                       @PathVariable Long postId,
                                       @PathVariable Long commentId,
                                       @RequestAttribute Long userId) {
        groupCommentService.delete(groupId, postId, commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
