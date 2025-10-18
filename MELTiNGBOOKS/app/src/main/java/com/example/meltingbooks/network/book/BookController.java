package com.example.meltingbooks.network.book;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.meltingbooks.network.ApiClient;

import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.feed.FeedResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class BookController {

    private static final String TAG = "BookController";
    private BookApi bookApi;
    //token 멤버 변수로 선언
    private String token;
    private ApiService apiService;

    public BookController(Context context) {
        // SharedPreferences에서 토큰 가져오기
        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        token = prefs.getString("jwt", null);

        if (token == null) {
            Log.e(TAG, "JWT 토큰이 없습니다.");
            return;
        }

        bookApi = ApiClient.getClient(token).create(BookApi.class);
        apiService = ApiClient.getClient(token).create(ApiService.class);
    }

    // 전체 책 리스트 가져오기
    public void fetchBooks(Callback<List<Book>> callback) {
        if (bookApi == null) return;

        Call<List<Book>> call = bookApi.getAllBooks();
        call.enqueue(callback);
    }

    // 책 검색
    public void searchBooks(String keyword, Callback<BookResponse> callback) {
        if (bookApi == null) return;

        Call<BookResponse> call = bookApi.searchBooks(keyword);
        call.enqueue(callback);
    }

    // bookId로 책 상세 조회
    public void getBookDetail(Integer bookId, Callback<Book> callback) {
        if (bookApi == null) return;

        Call<Book> call = bookApi.getBookDetail(null, bookId);
        call.enqueue(callback);
    }

    //책 생성
    public void createBook(BookCreateRequest request, Callback<Book> callback) {
        if (bookApi == null) return;

        Call<Book> call = bookApi.createBook("Bearer " + token, request);
        call.enqueue(callback);


    }

    // ⭐ 인기 책 가져오기
    public void fetchPopularBooks(Callback<List<Book>> callback) {
        if (bookApi == null) return;
        bookApi.getPopularBooks().enqueue(callback);
    }


    // ⭐ 책별 리뷰 가져오기
    public void fetchReviewsByBook(Integer bookId, Callback<ApiResponse<List<FeedResponse>>> callback) {
        if (apiService == null) return;
        apiService.getReviewsByBook(bookId).enqueue(callback);
    }



    /*FeedPageResponse 사용버전
    // ⭐ 책별 리뷰 가져오기
    public void fetchReviewsByBook(Integer bookId,
                                   Callback<ApiResponse<FeedPageResponse>> callback) {
        if (apiService == null) return;

        Call<ApiResponse<FeedPageResponse>> call = apiService.getReviewsByBook(bookId);
        call.enqueue(callback);
    }*/

    public void searchBooksWithSort(String keyword, String sort, Callback<BookResponse> callback) {
        bookApi.searchBooksBySort(keyword, sort).enqueue(callback); // Retrofit API 호출
    }

}
