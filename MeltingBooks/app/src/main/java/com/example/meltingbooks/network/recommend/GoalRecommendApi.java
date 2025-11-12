package com.example.meltingbooks.network.recommend;

import com.example.meltingbooks.network.ApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface GoalRecommendApi {

    @GET("/api/users/{userId}/recommend/goals")
    Call<ApiResponse<GoalRecommendResponse>> getGoalRecommendation(
            @Header("Authorization") String token,
            @Path("userId") int userId
    );
}
