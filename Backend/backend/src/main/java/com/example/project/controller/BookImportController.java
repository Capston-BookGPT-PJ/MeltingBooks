package com.example.project.controller;

import com.example.project.dto.BookDto;
import com.example.project.dto.ExternalBookDto;
import com.example.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books/import")
@RequiredArgsConstructor
public class BookImportController {

    private final BookService bookService;

    // 외부(알라딘 등) DTO 업서트
    @PostMapping
    public ResponseEntity<BookDto> upsertFromExternal(@RequestBody ExternalBookDto dto) {
        return ResponseEntity.ok(bookService.upsertFromExternal(dto));
    }

    // ISBN13 으로 알라딘에서 가져와 저장
    @PostMapping("/aladin/{isbn13}")
    public ResponseEntity<BookDto> importFromAladin(@PathVariable String isbn13) {
        return ResponseEntity.ok(bookService.importFromAladinByIsbn13(isbn13));
    }
}
