package com.example.meltingbooks.network.feed;

import java.util.List;

public class FeedPageResponse {
    private List<FeedResponse> content;
    private Pageable pageable;
    private int totalPages;
    private int totalElements;
    private boolean last;
    private int size;
    private int number;
    private Sort sort;
    private int numberOfElements;
    private boolean first;
    private boolean empty;

    // Getter
    public List<FeedResponse> getContent() { return content; }
    public Pageable getPageable() { return pageable; }
    public int getTotalPages() { return totalPages; }
    public int getTotalElements() { return totalElements; }
    public boolean isLast() { return last; }
    public int getSize() { return size; }
    public int getNumber() { return number; }
    public Sort getSort() { return sort; }
    public int getNumberOfElements() { return numberOfElements; }
    public boolean isFirst() { return first; }
    public boolean isEmpty() { return empty; }

    // 내부 클래스들
    public static class Pageable {
        private Sort sort;
        private int offset;
        private int pageNumber;
        private int pageSize;
        private boolean paged;
        private boolean unpaged;

        public Sort getSort() { return sort; }
        public int getOffset() { return offset; }
        public int getPageNumber() { return pageNumber; }
        public int getPageSize() { return pageSize; }
        public boolean isPaged() { return paged; }
        public boolean isUnpaged() { return unpaged; }
    }

    public static class Sort {
        private boolean empty;
        private boolean sorted;
        private boolean unsorted;

        public boolean isEmpty() { return empty; }
        public boolean isSorted() { return sorted; }
        public boolean isUnsorted() { return unsorted; }
    }
}
