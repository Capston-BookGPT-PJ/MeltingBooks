package com.example.project.service;

import com.example.project.dto.BookDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AladinService {

    private final RestTemplate restTemplate;

    @Value("${aladin.api.key}")
    private String apiKey;

    /**
     * 알라딘 API 기반 검색 (정렬 지원)
     * sort: popular | rating | latest
     * page: 0부터 시작
     * size: 1~100 권장
     */
    public List<BookDto> searchBooks(String query, String sort, int page, int size) {
        try {
            String aladinSort = mapSort(sort);

            int safeSize = Math.min(Math.max(1, size), 100);
            int start = Math.max(1, page * safeSize + 1);

            String url = UriComponentsBuilder.fromHttpUrl("https://www.aladin.co.kr/ttb/api/ItemSearch.aspx")
                    .queryParam("ttbkey", apiKey)
                    .queryParam("Query", query == null ? "" : query)
                    .queryParam("QueryType", "Title")
                    .queryParam("SearchTarget", "Book")
                    .queryParam("Start", start)
                    .queryParam("MaxResults", safeSize)
                    .queryParam("Sort", aladinSort) // SalesPoint | CustomerRating | PublishTime
                    .queryParam("output", "js")
                    .queryParam("Version", "20131101")
                    .build()
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode items = root.path("item");

            List<BookDto> books = new ArrayList<>();
            if (items.isArray()) {
                for (JsonNode item : items) {
                    books.add(parseBook(item));
                }
            }
            return books;

        } catch (Exception e) {
            throw new RuntimeException("알라딘 API 호출/파싱 중 오류 발생", e);
        }
    }

    /** 기본 popular, page=0, size=10 */
    public List<BookDto> searchBooks(String query) {
        return searchBooks(query, "popular", 0, 10);
    }

    /** ✅ ISBN13 단권 조회 (BookService.importFromAladinByIsbn13 에서 사용) */
    public BookDto fetchByIsbn13(String isbn13) {
        if (isbn13 == null || isbn13.isBlank()) return null;
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://www.aladin.co.kr/ttb/api/ItemLookUp.aspx")
                    .queryParam("ttbkey", apiKey)
                    .queryParam("ItemIdType", "ISBN13")
                    .queryParam("ItemId", isbn13)
                    .queryParam("output", "js")
                    .queryParam("Version", "20131101")
                    .build()
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode items = root.path("item");
            if (items.isArray() && items.size() > 0) {
                return parseBook(items.get(0));
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("알라딘 단권 조회 실패 (ISBN13=" + isbn13 + ")", e);
        }
    }

    /** 공통 파서 */
    private BookDto parseBook(JsonNode item) {
        String link = item.path("link").asText("");
        // 알라딘 응답에 종종 &amp; 형태가 있어 가독성 위해 교정
        if (link != null) link = link.replace("&amp;", "&");

        return BookDto.builder()
                .title(item.path("title").asText(""))
                .author(item.path("author").asText(""))
                .publisher(item.path("publisher").asText(""))
                .pubDate(item.path("pubDate").asText(""))
                .isbn(item.path("isbn").asText(""))
                .isbn13(item.path("isbn13").asText(""))
                .cover(item.path("cover").asText(""))
                .link(link)
                .categoryName(item.path("categoryName").asText(""))
                .itemPage(item.path("itemPage").asInt(0))
                .build();
    }

    private String mapSort(String sort) {
        if (sort == null) return "SalesPoint";
        return switch (sort.toLowerCase()) {
            case "rating" -> "CustomerRating";
            case "latest" -> "PublishTime";
            case "popular" -> "SalesPoint";
            default -> "SalesPoint";
        };
    }
}
