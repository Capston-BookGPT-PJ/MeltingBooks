package com.example.meltingbooks.network.recommend;

import com.example.meltingbooks.network.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RecommendBookApi {

    // GET /api/users/{userId}/recommend/books?page=0&size=5
    @GET("/api/users/{userId}/recommend/books")
    Call<ApiResponse<RecommendBookPageResponse>> getRecommendedBooks(
            @Header("Authorization") String token,
            @Path("userId") int userId,
            @Query("page") int page,
            @Query("size") int size
    );
}
