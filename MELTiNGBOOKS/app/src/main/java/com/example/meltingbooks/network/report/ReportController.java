package com.example.meltingbooks.network.report;

import android.util.Log;

import com.example.meltingbooks.network.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportController {

    private final ReportApi api;

    public ReportController(ReportApi api) {
        this.api = api;
    }

    public interface ReportCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public void getMonthlyReport(String token, int year, int month, ReportCallback<ReportResponse> callback) {
        api.getMonthlyReport("Bearer " + token, year, month)
                .enqueue(new Callback<ApiResponse<ReportResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ReportResponse>> call, Response<ApiResponse<ReportResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError("데이터를 불러올 수 없습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReportResponse>> call, Throwable t) {
                        Log.e("ReportController", "getMonthlyReport error: " + t.getMessage());
                        callback.onError("네트워크 오류가 발생했습니다.");
                    }
                });
    }

    public void getYearlyReport(String token, int year, ReportCallback<ReportResponse> callback) {
        api.getYearlyReport("Bearer " + token, year)
                .enqueue(new Callback<ApiResponse<ReportResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ReportResponse>> call, Response<ApiResponse<ReportResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError("데이터를 불러올 수 없습니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReportResponse>> call, Throwable t) {
                        Log.e("ReportController", "getYearlyReport error: " + t.getMessage());
                        callback.onError("네트워크 오류가 발생했습니다.");
                    }
                });
    }
}
