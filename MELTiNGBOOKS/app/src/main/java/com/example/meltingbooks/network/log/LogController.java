package com.example.meltingbooks.network.log;

import com.example.meltingbooks.network.ApiResponse;

import java.util.List;

import retrofit2.Callback;

public class LogController {
    private final LogApi logApi;

    //Context 대신 LogApi를 외부에서 주입
    public LogController(LogApi logApi) {
        this.logApi = logApi;
    }

    public void getLogsByPeriod(String token, int userId, String from, String to,
                                Callback<ApiResponse<List<ReadingLogResponse>>> callback) {
        logApi.getLogsByPeriod("Bearer " + token, userId, from, to).enqueue(callback);
    }


    public void createLog(String token, int userId, int bookId, ReadingLogRequest request,
                          Callback<ApiResponse<ReadingLogResponse>> callback) {
        logApi.createLog("Bearer " + token, userId, bookId, request).enqueue(callback);
    }

    public void updateLog(String token, int userId, int logId, ReadingLogRequest request,
                          Callback<ApiResponse<ReadingLogResponse>> callback) {
        logApi.updateLog("Bearer " + token, userId, logId, request).enqueue(callback);
    }

    public void deleteLog(String token, int userId, int logId,
                          Callback<Void> callback) {
        logApi.deleteLog("Bearer " + token, userId, logId).enqueue(callback);
    }

}
