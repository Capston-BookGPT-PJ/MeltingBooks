package com.example.meltingbooks.network.report;

import com.example.meltingbooks.network.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ReportApi {
    @GET("/api/reports/monthly")
    Call<ApiResponse<ReportResponse>>getMonthlyReport(
            @Header("Authorization") String token,
            @Query("year") int year,
            @Query("month") int month
    );

    @GET("/api/reports/yearly")
    Call<ApiResponse<ReportResponse>> getYearlyReport(
            @Header("Authorization") String token,
            @Query("year") int year
    );
}
