package com.example.meltingbooks.network.group.goal;

import com.example.meltingbooks.network.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupGoalController {

    private final GroupGoalApi apiService;

    public GroupGoalController(GroupGoalApi apiService) {
        this.apiService = apiService;
    }

    /** ✅ 그룹 목표 등록 */
    public void createGroupGoal(int groupId, GroupGoalRequest request, GoalCallback<GroupGoalResponse> callback) {
        apiService.createGroupGoal(groupId, request).enqueue(new Callback<GroupGoalResponse>() {
            @Override
            public void onResponse(Call<GroupGoalResponse> call, Response<GroupGoalResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Create Goal failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GroupGoalResponse> call, Throwable t) {
                callback.onError("Create Goal error: " + t.getMessage());
            }
        });
    }

    /** ✅ 그룹 목표 조회 */
    public void getGroupGoals(int groupId, GoalCallback<List<GroupGoalResponse>> callback) {
        apiService.getGroupGoals(groupId).enqueue(new Callback<ApiResponse<List<GroupGoalResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<GroupGoalResponse>>> call,
                                   Response<ApiResponse<List<GroupGoalResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<GroupGoalResponse>> apiResponse = response.body();
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
            public void onFailure(Call<ApiResponse<List<GroupGoalResponse>>> call, Throwable t) {
                callback.onError("Get Goals error: " + t.getMessage());
            }
        });
    }

    /** ✅ 그룹 목표 삭제 */
    public void deleteGroupGoal(int groupId, int goalId, GoalCallback<Void> callback) {
        apiService.deleteGroupGoal(groupId, goalId).enqueue(new Callback<Void>() {
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

    /** ✅ 그룹 목표 달성률 재계산 */
    public void recomputeGroupGoal(int groupId, int goalId, GoalCallback<Void> callback) {
        apiService.recomputeGroupGoal(groupId, goalId).enqueue(new Callback<Void>() {
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

    /** ✅ 공통 콜백 인터페이스 */
    public interface GoalCallback<T> {
        void onSuccess(T result);
        void onError(String errorMsg);
    }
}
