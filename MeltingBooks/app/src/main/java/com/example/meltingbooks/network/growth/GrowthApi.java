package com.example.meltingbooks.network.growth;

import retrofit2.Call;

import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GrowthApi {

    @POST("/api/level/give-exp")
    Call<String> giveExp(
            @Header("Authorization") String token,
            @Query("userId") int userId,
            @Query("eventType") String eventType
    );

}
