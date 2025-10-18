package com.example.meltingbooks.network.log;

import com.example.meltingbooks.network.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LogApi {
    @GET("/api/users/{userId}/logs")
    Call<ApiResponse<List<ReadingLogResponse>>> getLogsByPeriod(
            @Header("Authorization") String token,
            @Path("userId") int userId,
            @Query("from") String from,
            @Query("to") String to
    );

    @POST("/api/users/{userId}/logs/books/{bookId}")
    Call<ApiResponse<ReadingLogResponse>> createLog(
            @Header("Authorization") String token,
            @Path("userId") int userId,
            @Path("bookId") int bookId,
            @Body ReadingLogRequest request
    );

    @PUT("/api/users/{userId}/logs/{logId}")
    Call<ApiResponse<ReadingLogResponse>> updateLog(
            @Header("Authorization") String token,
            @Path("userId") int userId,
            @Path("logId") int logId,
            @Body ReadingLogRequest request
    );

    @DELETE("/api/users/{userId}/logs/{logId}")
    Call<Void> deleteLog(
            @Header("Authorization") String token,
            @Path("userId") int userId,
            @Path("logId") int logId
    );


}