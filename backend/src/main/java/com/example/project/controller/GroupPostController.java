package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.GroupPostDto;
import com.example.project.dto.GroupPostCreateRequest;
import com.example.project.dto.GroupPostUpdateRequest;
import com.example.project.service.GroupPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/posts")
@RequiredArgsConstructor
public class GroupPostController {

    private final GroupPostService groupPostService;

    /* ---------- 글 생성: JSON(raw) ---------- */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ApiResponse<GroupPostDto>> createPostJson(
            @PathVariable Long groupId,
            @RequestAttribute Long userId,
            @RequestBody GroupPostCreateRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                groupPostService.createPostJson(groupId, userId, req)
        ));
    }

    /* ---------- 글 수정: JSON(raw) ---------- */
    @PutMapping(value="/{postId}", consumes = "application/json")
    public ResponseEntity<ApiResponse<GroupPostDto>> updatePostJson(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestAttribute Long userId,
            @RequestBody GroupPostUpdateRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                groupPostService.updatePostJson(postId, userId, req)
        ));
    }

    /* ---------- 이미지 추가 업로드 (여러 장, append) ---------- */
    @PostMapping(value="/{postId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GroupPostDto>> addImages(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestAttribute Long userId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                groupPostService.addImages(postId, userId, files)
        ));
    }

    /* ---------- 이미지 전체 교체 (옵션) ---------- */
    @PutMapping(value="/{postId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GroupPostDto>> replaceImages(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestAttribute Long userId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                groupPostService.replaceImages(postId, userId, files)
        ));
    }

    /* ---------- 특정 이미지 삭제 ---------- */
    @DeleteMapping("/{postId}/images/{imageId}")
    public ResponseEntity<ApiResponse<GroupPostDto>> removeImage(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @PathVariable Long imageId,
            @RequestAttribute Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                groupPostService.removeImage(postId, userId, imageId)
        ));
    }
    
        @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestAttribute Long userId
    ) {
        groupPostService.deletePost(groupId, postId, userId);
        return ResponseEntity.noContent().build(); // 204
    }

    /* ---------- 좋아요/취소는 그대로 ---------- */
    @PostMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<Void>> likePost(@PathVariable Long postId,
                                                      @RequestAttribute Long userId) {
        groupPostService.likePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<Void>> unlikePost(@PathVariable Long postId,
                                                        @RequestAttribute Long userId) {
        groupPostService.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }
    
    // 단건 조회
@GetMapping("/{postId}")
public ResponseEntity<ApiResponse<GroupPostDto>> getPost(@PathVariable Long groupId,
                                                         @PathVariable Long postId,
                                                         @RequestAttribute(required = false) Long userId) {
    // groupId는 경로 정합성 체크용으로만 쓰고, 실제 조회는 postId로 처리
    GroupPostDto dto = groupPostService.getPost(postId, userId);
    return ResponseEntity.ok(ApiResponse.success(dto));
}

}
