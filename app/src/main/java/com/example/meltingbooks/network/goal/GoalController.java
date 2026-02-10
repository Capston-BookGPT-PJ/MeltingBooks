package com.example.meltingbooks.network.goal;

import com.example.meltingbooks.network.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalController {
    private final GoalApi apiService;

    public GoalController(GoalApi apiService) {
        this.apiService = apiService;
    }


    // 목표 등록/수정
    public void saveGoal(String token, GoalRequest request, Integer goalId, GoalCallback<GoalResponse> callback) {
        Call<GoalResponse> call;
        if (goalId == null) { // 새로 등록
            // call = apiService.createGoal(request, "Bearer " + token);
            call = apiService.createGoal(request);
        } else { // 수정
            //call = apiService.updateGoal(goalId, request, "Bearer " + token);
            call = apiService.updateGoal(goalId, request);
        }

        call.enqueue(new Callback<GoalResponse>() {
            @Override
            public void onResponse(Call<GoalResponse> call, Response<GoalResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Save Goal failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GoalResponse> call, Throwable t) {
                callback.onError("Save Goal error: " + t.getMessage());
            }
        });
    }

    // 목표 조회
    public void getGoals(String token, GoalCallback<List<GoalResponse>> callback) {
        apiService.getGoals()  // 토큰 안 쓰는 경우 그대로
                .enqueue(new Callback<ApiResponse<List<GoalResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<GoalResponse>>> call,
                                           Response<ApiResponse<List<GoalResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<GoalResponse>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                callback.onError("Get Goals failed: " + apiResponse.getError());
                            }
                        } else {
                            callback.onError("Get Goals failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<GoalResponse>>> call, Throwable t) {
                        callback.onError("Get Goals error: " + t.getMessage());
                    }
                });
    }

    // 목표 삭제
    public void deleteGoal(String token, int goalId, GoalCallback<Void> callback) {

        apiService.deleteGoal(goalId).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Delete Goal failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Delete Goal error: " + t.getMessage());
            }
        });
    }

    // 목표 달성률 재계산
    public void recomputeGoal(int goalId, GoalCallback<Void> callback) {
        apiService.recomputeGoal(goalId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Recompute failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Recompute error: " + t.getMessage());
            }
        });
    }

    // 공통 콜백 인터페이스
    public interface GoalCallback<T> {
        void onSuccess(T result);
        void onError(String errorMsg);
    }
}