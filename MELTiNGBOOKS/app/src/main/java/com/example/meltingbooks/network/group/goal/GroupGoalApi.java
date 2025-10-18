package com.example.meltingbooks.network.group.goal;

import com.example.meltingbooks.network.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface GroupGoalApi {
    /** ✅ 그룹 목표 생성 */
    @POST("/api/groups/{groupId}/goals")
    Call<GroupGoalResponse> createGroupGoal(
            @Path("groupId") int groupId,
            @Body GroupGoalRequest request
    );

    /** ✅ 그룹 목표 조회 */
    @GET("/api/groups/{groupId}/goals")
    Call<ApiResponse<List<GroupGoalResponse>>> getGroupGoals(
            @Path("groupId") int groupId
    );

    /** ✅ 그룹 목표 삭제 */
    @DELETE("/api/groups/{groupId}/goals/{goalId}")
    Call<Void> deleteGroupGoal(
            @Path("groupId") int groupId,
            @Path("goalId") int goalId
    );

    /** ✅ 그룹 목표 달성률 재계산 */
    @POST("/api/groups/{groupId}/goals/{goalId}/recompute")
    Call<Void> recomputeGroupGoal(
            @Path("groupId") int groupId,
            @Path("goalId") int goalId
    );

}