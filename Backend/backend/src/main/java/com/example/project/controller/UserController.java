package com.example.project.controller;

import com.example.project.dto.BasicUserDto;
import com.example.project.dto.UserCreateRequest;
import com.example.project.dto.UserDto;
import com.example.project.entity.User;
import com.example.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 전체 사용자 조회 (그룹명 3개 주입된 상세 DTO)
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers().stream()
                .map(userService::toUserDtoWithGroups)
                .toList();
        return ResponseEntity.ok(users);
    }

    // 사용자 단건 조회 (그룹명 3개 주입된 상세 DTO)
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userService.toUserDtoWithGroups(user));
    }

    // 사용자 생성
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreateRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setUsername(request.getUsername());
        user.setProfileImageUrl(request.getProfileImageUrl());
        user.setBio(request.getBio());
        user.setTagId(request.getTagId());

        User saved = userService.createUser(user);
        return ResponseEntity.ok(userService.toUserDtoWithGroups(saved));
    }

    // 사용자 수정
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserCreateRequest request) {
        User updating = new User();
        updating.setEmail(request.getEmail());
        updating.setNickname(request.getNickname());
        updating.setUsername(request.getUsername());
        updating.setProfileImageUrl(request.getProfileImageUrl());
        updating.setBio(request.getBio());
        updating.setTagId(request.getTagId());

        User updated = userService.updateUser(id, updating);
        return ResponseEntity.ok(userService.toUserDtoWithGroups(updated));
    }

    // 사용자 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ 닉네임 검색 (부분 일치, 대소문자 무시) — 경량 DTO 반환
    @GetMapping("/search")
    public ResponseEntity<List<BasicUserDto>> searchUsers(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.searchByNickname(nickname));
    }

    // ✅ 인기 유저 Top 10 (팔로워 수 많은 순) — 경량 DTO 반환
    @GetMapping("/popular")
    public ResponseEntity<List<BasicUserDto>> getPopularUsers() {
        return ResponseEntity.ok(userService.getPopularUsers());
    }
}
