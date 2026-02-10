package com.example.meltingbooks.network.browse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UserApi {
    //인기 유저 조회
    /**@GET("/api/users/popular")
    Call<List<PopularUser>> getPopularUsers(
            @Header("Authorization") String token
    );*/

    @GET("/api/users/popular")
    Call<List<PopularUser>> getPopularUsers();

    @GET("/api/users/search")
    Call<List<PopularUser>> searchUsers(@Query("nickname") String nickname);

}