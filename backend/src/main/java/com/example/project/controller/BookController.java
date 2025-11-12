package com.example.project.controller;

import com.example.project.dto.BookDto;
import com.example.project.entity.Book;
import com.example.project.service.AladinService;
import com.example.project.service.BookService;
import com.example.project.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.net.URI;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    private final AladinService aladinService;

    // 책 저장
    @PostMapping
    public ResponseEntity<BookDto> saveBook(@RequestBody BookDto dto) {
        BookDto saved = bookService.saveBook(dto);
        return ResponseEntity
                .created(URI.create("/api/books/" + saved.getBookId()))
                .body(saved);
    }

    // 모든 책 조회
    @GetMapping
    public ResponseEntity<List<BookDto>> getAllBooks() {
        return ResponseEntity.ok(bookService.findAll());
    }

    // 특정 책 조회
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    // 알라딘 검색
    @GetMapping("/search")
public ApiResponse<List<BookDto>> searchBooks(
        @RequestParam String q,
        @RequestParam(required = false, defaultValue = "popular") String sort
) {
    return ApiResponse.success(bookService.searchBooksAdvanced(q, sort));
}

    // 인기 책
    @GetMapping("/popular")
    public ResponseEntity<List<BookDto>> getPopularBooks() {
        return ResponseEntity.ok(bookService.findAll()); // 이미 정렬 포함됨
    }

    // 고도화 검색
    @GetMapping("/search-advanced")
    public ResponseEntity<List<BookDto>> searchBooksAdvanced(
            @RequestParam String keyword,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(bookService.searchBooksAdvanced(keyword, sort));
    }
}
