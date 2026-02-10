package com.example.meltingbooks.network.profile;

import com.example.meltingbooks.network.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FollowApi {
    //팔로잉 조회
    @GET("api/follows/following/{userId}")
    Call<ApiResponse<List<FollowUser>>> getFollowing(
            @Header("Authorization") String token,
            @Path("userId") int userId
    );

    //팔로워 조회
    @GET("api/follows/followers/{userId}")
    Call<ApiResponse<List<FollowUser>>> getFollowers(
            @Header("Authorization") String token,
            @Path("userId") int userId
    );

    //팔로잉
    @POST("/api/follows")
    Call<ApiResponse<String>> followUser(
            @Header("Authorization") String token,
            @Query("followerId") int followerId,
            @Query("followingId") int followingId
    );

    //언 팔로잉
    @DELETE("/api/follows")
    Call<Void> unfollowUser(
            @Header("Authorization") String token,
            @Query("followerId") int followerId,
            @Query("followingId") int followingId
    );
}
