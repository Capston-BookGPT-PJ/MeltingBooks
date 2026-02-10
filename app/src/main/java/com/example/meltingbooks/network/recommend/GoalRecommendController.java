package com.example.meltingbooks.network.recommend;

import com.example.meltingbooks.network.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalRecommendController {

    private final GoalRecommendApi api;

    public GoalRecommendController(GoalRecommendApi api) {
        this.api = api;
    }

    public interface RecommendCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void getGoalRecommendation(String token, int userId, RecommendCallback<GoalRecommendResponse> callback) {
        api.getGoalRecommendation("Bearer " + token, userId)
                .enqueue(new Callback<ApiResponse<GoalRecommendResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<GoalRecommendResponse>> call,
                                           Response<ApiResponse<GoalRecommendResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError("추천 데이터 없음 또는 서버 오류");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<GoalRecommendResponse>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }
}
