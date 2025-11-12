package com.example.project.service;

import com.example.project.dto.BookDto;
import com.example.project.dto.ExternalBookDto;
import com.example.project.entity.Book;
import com.example.project.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AladinService aladinService;

    /** ISBN13 기준 업서트 (내부 DTO) */
    @Transactional
    public BookDto saveBook(BookDto dto) {
        if (dto == null || isBlank(dto.getIsbn13())) {
            throw new IllegalArgumentException("ISBN13은 필수입니다.");
        }
        Book saved = bookRepository.findByIsbn13(dto.getIsbn13())
                .map(existing -> {
                    existing.setTitle(n(dto.getTitle(), existing.getTitle()));
                    existing.setAuthor(n(dto.getAuthor(), existing.getAuthor()));
                    existing.setPublisher(n(dto.getPublisher(), existing.getPublisher()));
                    existing.setPubDate(n(dto.getPubDate(), existing.getPubDate()));
                    existing.setIsbn(n(dto.getIsbn(), existing.getIsbn()));
                    existing.setCover(n(dto.getCover(), existing.getCover()));
                    existing.setLink(n(dto.getLink(), existing.getLink()));
                    existing.setCategoryName(n(dto.getCategoryName(), existing.getCategoryName()));
                    if (dto.getItemPage() > 0) existing.setItemPage(dto.getItemPage());
                    return existing;
                })
                .orElseGet(() -> bookRepository.save(dto.toEntity()));
        return BookDto.from(saved);
    }

    /** ������ 외부(알라딘 등) DTO 업서트: 컨트롤러에서 호출하는 메서드 */
    @Transactional
    public BookDto upsertFromExternal(ExternalBookDto dto) {
        if (dto == null) throw new IllegalArgumentException("payload가 비었습니다.");

        String isbn13 = trim(dto.getIsbn13());
        Book book = null;

        // 1) isbn13으로 먼저 조회
        if (!isBlank(isbn13)) {
            book = bookRepository.findByIsbn13(isbn13).orElse(null);
        }

        if (book == null) {
            // 2) 없으면 새로 생성
            book = Book.builder()
                    .title(n(dto.getTitle(), ""))
                    .author(n(dto.getAuthor(), ""))
                    .publisher(n(dto.getPublisher(), ""))
                    .pubDate(n(dto.getPubDate(), ""))
                    .isbn(n(dto.getIsbn(), ""))
                    .isbn13(n(isbn13, ""))
                    .cover(n(dto.getCover(), ""))
                    .link(n(dto.getLink(), ""))
                    .categoryName(n(dto.getCategoryName(), ""))
                    .itemPage(nzPages(dto.getItemPage()))
                    .build();
        } else {
            // 3) 기존 도서 업데이트(값이 있으면 덮어쓰기)
            book.setTitle(n(dto.getTitle(), book.getTitle()));
            book.setAuthor(n(dto.getAuthor(), book.getAuthor()));
            book.setPublisher(n(dto.getPublisher(), book.getPublisher()));
            book.setPubDate(n(dto.getPubDate(), book.getPubDate()));
            book.setIsbn(n(dto.getIsbn(), book.getIsbn()));
            book.setIsbn13(n(isbn13, book.getIsbn13()));
            book.setCover(n(dto.getCover(), book.getCover()));
            book.setLink(n(dto.getLink(), book.getLink()));
            book.setCategoryName(n(dto.getCategoryName(), book.getCategoryName()));
            int pages = nzPages(dto.getItemPage());
            if (pages > 0) book.setItemPage(pages);
        }

        Book saved = bookRepository.save(book);
        return BookDto.from(saved);
    }

    @Transactional(readOnly = true)
    public BookDto findById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다: " + id));
        return BookDto.from(book);
    }

    @Transactional(readOnly = true)
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .sorted(Comparator
                        .comparingInt((Book b) -> b.getReviews() == null ? 0 : b.getReviews().size())
                        .reversed())
                .map(BookDto::from)
                .collect(Collectors.toList());
    }

    /** 알라딘 기반 고급 검색 */
    @Transactional(readOnly = true)
    public List<BookDto> searchBooksAdvanced(String keyword, String sort) {
        String q = (keyword == null || keyword.isBlank()) ? "" : keyword;
        return aladinService.searchBooks(q, sort, 0, 20);
    }

    @Transactional(readOnly = true)
    public List<BookDto> searchBooksAdvanced(String keyword, String sort, int page, int size) {
        String q = (keyword == null || keyword.isBlank()) ? "" : keyword;
        return aladinService.searchBooks(q, sort, page, size);
    }

    /** ISBN13로 알라딘에서 가져와 DB 업서트 */
    @Transactional
    public BookDto importFromAladinByIsbn13(String isbn13) {
        if (isBlank(isbn13)) throw new IllegalArgumentException("ISBN13은 필수입니다.");
        return bookRepository.findByIsbn13(isbn13)
                .map(BookDto::from)
                .orElseGet(() -> {
                    BookDto ext = aladinService.fetchByIsbn13(isbn13);
                    if (ext == null) throw new RuntimeException("알라딘에서 책을 찾지 못했습니다: " + isbn13);
                    return saveBook(ext);
                });
    }

    /* ---------- helpers ---------- */
    private static String n(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String trim(String s) { return s == null ? null : s.trim(); }
    private static int nzPages(Integer v) { return v == null ? 0 : Math.max(0, v); }
}
